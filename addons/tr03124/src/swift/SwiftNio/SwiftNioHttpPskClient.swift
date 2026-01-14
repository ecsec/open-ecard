import AsyncHTTPClient
import Foundation
import HTTPTypes
import NIO
import NIOExtras
import NIOHTTPTypes
import NIOHTTPTypesHTTP1
import NIOSSL

@objc
public final class OpenSslTlsChannelHandler: NSObject, ChannelDuplexHandler {
    public typealias InboundIn = ByteBuffer
    public typealias InboundOut = ByteBuffer
    public typealias OutboundIn = ByteBuffer
    public typealias OutboundOut = IOData

    let handshake: () -> Void
    let inboundEncrypted: (NSData?) -> Void
    let readPlaintext: () -> NSData?
    let outboundPlain: (NSData?) -> Void
    let readEncryptedOutput: () -> NSData?
    let closeNotify: () -> Void

    var encryptedBuffer: [ByteBuffer] = []

    init(
        handshake: @escaping () -> Void,
        inboundEncrypted: @escaping (NSData?) -> Void,
        readPlaintext: @escaping () -> NSData?,
        outboundPlain: @escaping (NSData?) -> Void,
        readEncryptedOutput: @escaping () -> NSData?,
        closeNotify: @escaping () -> Void
    ) {
        self.handshake = handshake
        self.inboundEncrypted = inboundEncrypted
        self.readPlaintext = readPlaintext
        self.outboundPlain = outboundPlain
        self.readEncryptedOutput = readEncryptedOutput
        self.closeNotify = closeNotify
    }

    @objc public static func create(
        handshake: @escaping () -> Void,
        inboundEncrypted: @escaping (NSData?) -> Void,
        readPlaintext: @escaping () -> NSData?,
        outboundPlain: @escaping (NSData?) -> Void,
        readEncryptedOutput: @escaping () -> NSData?,
        closeNotify: @escaping () -> Void
    ) -> OpenSslTlsChannelHandler {
        return OpenSslTlsChannelHandler(
            handshake: handshake,
            inboundEncrypted: inboundEncrypted,
            readPlaintext: readPlaintext,
            outboundPlain: outboundPlain,
            readEncryptedOutput: readEncryptedOutput,
            closeNotify: closeNotify
        )
    }

    public func channelRead(context: ChannelHandlerContext, data: NIOAny) {
        var buffer = unwrapInboundIn(data)
        let bytes = buffer.readBytes(length: buffer.readableBytes) ?? []

        inboundEncrypted(NSData(bytes: bytes, length: bytes.count))

        var readData = false
        while let plain = readPlaintext() {
            readData = true
            context.fireChannelRead(wrapInboundOut(ByteBuffer(bytes: plain)))
        }
        flushIntern(context: context, promise: nil)
        if readData {
            context.fireChannelReadComplete()
        }

    }

    public func write(
        context: ChannelHandlerContext, data: NIOAny,
        promise: EventLoopPromise<Void>?
    ) {
        var buffer = unwrapOutboundIn(data)
        let bytes = buffer.readBytes(length: buffer.readableBytes) ?? []
        outboundPlain(NSData(bytes: bytes, length: bytes.count))
        flushIntern(context: context, promise: promise)
    }

    public func flush(context: ChannelHandlerContext) {
        flushIntern(context: context, promise: nil)
    }
    private func flushIntern(
        context: ChannelHandlerContext, promise: EventLoopPromise<Void>?
    ) {
        // Drain OpenSSL
        while let enc = readEncryptedOutput() {
            encryptedBuffer.append(ByteBuffer(bytes: enc))
        }

        guard context.channel.isWritable else {
            promise?.fail(ChannelError.ioOnClosedChannel)
            return
        }

        let count = encryptedBuffer.count
        guard count > 0 else {
            promise?.succeed(())
            context.flush()
            return
        }

        for i in 0..<count {
            let buf = encryptedBuffer[i]
            let p = (i == count - 1) ? promise : nil
            context.write(wrapOutboundOut(IOData.byteBuffer(buf)), promise: p)
        }

        encryptedBuffer.removeAll()
        context.flush()
    }
    public func close(
        context: ChannelHandlerContext, mode: CloseMode,
        promise: EventLoopPromise<Void>?
    ) {
        closeNotify()
        flushIntern(context: context, promise: nil)
        context.eventLoop.execute {
            context.close(mode: mode, promise: promise)
        }
    }
    public func channelWritabilityChanged(context: ChannelHandlerContext) {
        flushIntern(context: context, promise: nil)
        context.fireChannelWritabilityChanged()
    }
}

enum HTTPPartialResponse {
    case none
    case receiving(HTTPResponse, ByteBuffer)
}
enum HTTPClientError: Error {
    case malformedResponse, unexpectedEndOfStream
}
@objc public class SwiftNioHttpPskClient: NSObject {

    private static func buildChannel(
        thrGroup: MultiThreadedEventLoopGroup,
        osslTlsChannelHandler: OpenSslTlsChannelHandler,
        host: String,
        httpHandlers: Bool
    ) async throws -> ClientBootstrap {

        return ClientBootstrap(group: thrGroup).channelInitializer {
            channel in
            channel.eventLoop.makeCompletedFuture {
                channel.pipeline.addHandler(osslTlsChannelHandler)
                try channel.pipeline.syncOperations.addHTTPClientHandlers()
                try channel.pipeline.syncOperations.addHandlers(
                    HTTP1ToHTTPClientCodec())

            }
        }

    }

    private static func bootStrapAsyncHttpChannel(
        host: String,
        port: Int,
        bs: ClientBootstrap
    ) async throws -> NIOAsyncChannel<HTTPResponsePart, HTTPRequestPart> {
        let clientChannel = try await bs.connect(host: host, port: port)
            .flatMapThrowing {
                channel in
                try NIOAsyncChannel(
                    wrappingChannelSynchronously: channel,
                    configuration: NIOAsyncChannel.Configuration(
                        inboundType: HTTPResponsePart.self,
                        outboundType: HTTPRequestPart.self
                    )
                )
            }.get()
        return clientChannel
    }

    private static func execHttp(
        clientChannel: NIOAsyncChannel<HTTPResponsePart, HTTPRequestPart>,
        host: String,
        method: String,
        body: String?,
        path: String?,
        headers: [String: String]
    ) async throws -> (HTTPResponse, ByteBuffer) {
        try await clientChannel.executeThenClose { inbound, outbound in

            var headerFields = HTTPFields(
                headers.compactMap { e in
                    if let name = HTTPField.Name(e.key) {
                        return HTTPField(name: name, value: e.value)
                    } else {
                        return nil
                    }

                }
            )

            var buf: ByteBuffer?
            //print("BUFFER in SWIFT \(body)")
            if let b = body {
                buf = clientChannel.channel.allocator.buffer(string: b)
                //headerFields.append( HTTPField(name: HTTPField.Name("Content-Type")!, value: "application/vnd.paos+xml"))
                headerFields.append(
                    HTTPField(
                        name: HTTPField.Name("Content-Length")!,
                        value: "\(buf!.readableBytes)"))
            }
            
            var validatedPath =
            if(path == nil || path?.count == 0 ) {
                "/"
            } else {
                path
            }

            try await outbound.write(
                .head(
                    HTTPRequest(
                        method: HTTPRequest.Method(rawValue: method)!,
                        scheme: "https",
                        authority: host,
                        path: validatedPath,
                        headerFields: headerFields
                    )
                )
            )

            if let b = buf {
                try await outbound.write(.body(b))
            }

            try await outbound.write(.end(nil))

            var partialResponse = HTTPPartialResponse.none

            for try await part in inbound {
                switch part {
                case .head(let head):
                    guard case .none = partialResponse else {
                        throw HTTPClientError.malformedResponse
                    }

                    let buffer = clientChannel.channel.allocator.buffer(
                        capacity: 0
                    )
                    partialResponse = .receiving(head, buffer)
                case .body(let buffer):
                    guard
                        case .receiving(let head, var existingBuffer) =
                            partialResponse
                    else {
                        throw HTTPClientError.malformedResponse
                    }

                    existingBuffer.writeImmutableBuffer(buffer)
                    partialResponse = .receiving(head, existingBuffer)
                case .end:
                    guard
                        case .receiving(let head, var buffer) =
                            partialResponse
                    else {
                        throw HTTPClientError.malformedResponse
                    }

                    return (head, buffer)

                }
            }
            throw HTTPClientError.unexpectedEndOfStream
        }
    }

    @objc public static func performHttp(
        host: String,
        port: Int,
        method: String?,
        path: String?,
        headers: [String: String]?,
        body: String?,
        osslTlsChannelHandler: OpenSslTlsChannelHandler,
        completion: @escaping (Int, [String: String], NSData?) -> Void,
        onError: @escaping (NSError) -> Void
    ) {
        Task {
            do {
                let thrGroup = MultiThreadedEventLoopGroup(numberOfThreads: 1)
                let bs = try await buildChannel(
                    thrGroup: thrGroup,
                    osslTlsChannelHandler: osslTlsChannelHandler,
                    host: host,
                    httpHandlers: true
                )
                let clientChannel = try await bootStrapAsyncHttpChannel(
                    host: host,
                    port: port,
                    bs: bs
                )

                var (head, buffer) = try await execHttp(
                    clientChannel: clientChannel,
                    host: host,
                    method: method ?? "GET",
                    body: body,
                    path: path ?? "/",
                    headers: headers ?? [:]
                )
                let body: NSData? =
                    if let data = buffer.readData(length: buffer.readableBytes)
                    {
                        NSData(data: data)
                    } else {
                        nil
                    }

                completion(
                    head.status.code,
                    Dictionary(
                        head.headerFields.map { e in
                            (e.name.canonicalName, e.value)
                        },
                        uniquingKeysWith: { a, b in "\(a), \(b)" }
                    ),
                    body

                )
            } catch {
				print("Error during perform: \(error)")
                onError(error as NSError)
            }
        }
    }
}

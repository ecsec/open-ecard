//
//  ContentView.swift
//  xCodeTest
//
//  Created by Florian Otto on 03.07.25.
//

import SwiftUI
import openecard_pcscIos

struct ContentView: View {
    var body: some View {
        VStack {
            Image(systemName: "globe")
                .imageScale(.large)
                .foregroundStyle(.tint)
            Text("Testing NFC")
        }
        .padding()
    }
}

#Preview {
    ContentView()
}

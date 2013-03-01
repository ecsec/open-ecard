


#ifndef RSCT_IFD_H
#define RSCT_IFD_H



#include "Platform.h"


extern "C" {
# include "ifdhandler.h"
}

#include "cjeca32.h"

#ifdef HAVE_PTHREAD_H
#include <pthread.h>
#endif

#include <map>
#include <string>
#include <inttypes.h>



/* Set structure elements aligment on bytes
 * http://gcc.gnu.org/onlinedocs/gcc/Structure_002dPacking-Pragmas.html */
#ifdef __APPLE__
#pragma pack(1)
#else
#pragma pack(push, 1)
#endif


typedef struct {
  uint8_t SAD;
  uint8_t DAD;
  uint16_t BufferLength;
  uint8_t buffer;
} MCTUniversal_t;


/* restore default structure elements alignment */
#ifdef __APPLE__
#pragma pack()
#else
#pragma pack(pop)
#endif





class IFDHandler {
protected:

  class Context {
  private:
    DWORD m_lun;
    CReader *m_reader;
#ifdef HAVE_PTHREAD_H
    pthread_mutex_t m_mutex;
#endif
    ICC_STATE m_icc_state;
    DWORD m_atr_length;

  public:
    std::string dataToFlash;
    std::string signatureToFlash;

    uint32_t moduleCount;
    cj_ModuleInfo *moduleList;

    uint32_t busId;
    uint32_t busPos;

    uint16_t vendorId;
    uint16_t productId;


  public:
    Context(DWORD lun, CReader *r);
    ~Context();

    DWORD getLun() const { return m_lun;};
    CReader *getReader() { return m_reader;};

    ICC_STATE *getState() { return &m_icc_state;};
    DWORD getAtrLength() const { return m_atr_length;};

    void lock();
    void unlock();
  };
  typedef std::map<DWORD, Context*> ContextMap;

public:
  IFDHandler();
  ~IFDHandler();

  int init();

  RESPONSECODE createChannel(DWORD Lun, DWORD Channel);
  RESPONSECODE createChannelByName(DWORD Lun, char *devName);
  RESPONSECODE closeChannel(DWORD Lun);

  RESPONSECODE getCapabilities(DWORD Lun, DWORD Tag, PDWORD Length, PUCHAR Value);
  RESPONSECODE setCapabilities (DWORD Lun, DWORD Tag, DWORD Length, PUCHAR Value);

  RESPONSECODE setProtocolParameters (DWORD Lun, DWORD Protocol, UCHAR Flags, UCHAR PTS1, UCHAR PTS2, UCHAR PTS3);
  RESPONSECODE powerICC (DWORD Lun, DWORD Action, PUCHAR Atr, PDWORD AtrLength);

  RESPONSECODE transmitToICC(DWORD Lun, SCARD_IO_HEADER SendPci,
                             PUCHAR TxBuffer, DWORD TxLength,
                             PUCHAR RxBuffer, PDWORD RxLength, PSCARD_IO_HEADER RecvPci);
  RESPONSECODE control(DWORD Lun,
                       DWORD controlCode,
                       PUCHAR TxBuffer,
                       DWORD TxLength,
                       PUCHAR RxBuffer,
                       DWORD RxLength,
                       PDWORD RxReturned);
  RESPONSECODE iccPresence (DWORD Lun);


protected:
  RESPONSECODE p10MctUniversal(Context *ctx,
                               MCTUniversal_t *uni,
                               PUCHAR RxBuffer,
                               DWORD RxLength,
                               PDWORD RxReturned);

  RESPONSECODE p10GetFeatures(Context *ctx,
                              DWORD Lun,
                              PUCHAR RxBuffer,
                              DWORD RxLength,
                              PDWORD RxReturned);

  int8_t _specialKeyUpdate(Context *ctx,
                           uint16_t cmd_len,
                           const uint8_t *cmd,
                           uint16_t *response_len,
                           uint8_t *response);

  int8_t _specialUploadMod(Context *ctx,
                           uint16_t cmd_len,
                           const uint8_t *cmd,
                           uint16_t *response_len,
                           uint8_t *response);

  int8_t _specialUploadSig(Context *ctx,
                           uint16_t cmd_len,
                           const uint8_t *cmd,
                           uint16_t *response_len,
                           uint8_t *response);

  int8_t _specialUploadFlash(Context *ctx,
                             uint16_t cmd_len,
                             const uint8_t *cmd,
                             uint16_t *response_len,
                             uint8_t *response);

  int8_t _specialUploadInfo(Context *ctx,
                            uint16_t cmd_len,
                            const uint8_t *cmd,
                            uint16_t *response_len,
                            uint8_t *response);

  int8_t _specialDeleteAllMods(Context *ctx,
                               uint16_t cmd_len,
                               const uint8_t *cmd,
                               uint16_t *response_len,
                               uint8_t *response);

  int8_t _specialShowAuth(Context *ctx,
                          uint16_t cmd_len,
                          const uint8_t *cmd,
                          uint16_t *response_len,
                          uint8_t *response);

  int8_t _specialGetModuleCount(Context *ctx,
                                uint16_t cmd_len,
                                const uint8_t *cmd,
                                uint16_t *response_len,
                                uint8_t *response);

  int8_t _specialGetModuleInfo(Context *ctx,
                               uint16_t cmd_len,
                               const uint8_t *cmd,
                               uint16_t *response_len,
                               uint8_t *response);

  int8_t _specialGetReaderInfo(Context *ctx,
                               uint16_t cmd_len,
                               const uint8_t *cmd,
                               uint16_t *response_len,
                               uint8_t *response);

  int8_t _special(Context *ctx,
                  uint16_t cmd_len,
                  const uint8_t *cmd,
                  uint16_t *response_len,
                  uint8_t *response);





private:

#ifdef HAVE_PTHREAD_H
  pthread_mutex_t m_contextMutex;
#endif
  ContextMap m_contextMap;

};


#endif


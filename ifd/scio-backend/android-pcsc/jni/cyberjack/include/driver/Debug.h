#ifndef ECA_DEBUG_H
#define ECA_DEBUG_H


#define DEBUG_MASK_COMMUNICATION_OUT   0x00000001
#define DEBUG_MASK_COMMUNICATION_IN    0x00000002
#define DEBUG_MASK_COMMUNICATION_ERROR 0x00000004
#define DEBUG_MASK_COMMUNICATION_INT   0x00000008
#define DEBUG_MASK_COMMUNICATION_INFO  0x00000010
#define DEBUG_MASK_TRANSLATION         0x00000100
#define DEBUG_MASK_RESULTS             0x00000200
#define DEBUG_MASK_INPUT               0x00010000
#define DEBUG_MASK_OUTPUT              0x00020000

#define DEBUG_MASK_CTAPI               0x00040000
#define DEBUG_MASK_IFD                 0x00080000
#define DEBUG_MASK_CJECOM              0x00100000
#define DEBUG_MASK_PPA                 0x00200000

#define DEBUG_MASK_GENERIC             0x80000000

#ifdef __cplusplus

  
class CJECA32_PRIVATE_CLASS CDebug
{
public:
    CDebug(unsigned int nLevelMask);
    void Out(const char *cDeviceName,
	     unsigned int nLevelMask,
             const char *cCaption,
             void *pData, uint32_t ulDataLen);

    /**
     * This method can be used to log arbitrary information. It is used just
     * like printf. It internally calls @ref CDebug::Out.
     */
    void varLog(const char *devName,
                unsigned int nLevelMask,
                const char *format, ...);

public:
	~CDebug(void);

#if defined(OS_LINUX) || defined(OS_DARWIN)
	void setLevelMask(unsigned int nLevelMask);
	void setLogFileName(const char *fname);
#endif
	
private:
   unsigned int m_nLevelMask;
#if defined(OS_LINUX) || defined(OS_DARWIN)
       char *m_logFileName;
#endif
};

extern CDebug CJECA32_PRIVATE_CLASS Debug;

#endif /* ifdef __cplusplus */


#ifdef __cplusplus
extern "C" {
#endif

  void rsct_debug_out(const char *cDeviceName, unsigned int nLevelMask,
		      const char *cCaption,
		      void *pData, uint32_t ulDataLen);

#ifdef __cplusplus
}
#endif


#endif


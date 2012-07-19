#include "Platform.h"

#include <stdio.h>

#include "Debug.h"

#include <stdarg.h>



void CDebug::varLog(const char *devName,
                    unsigned int nLevelMask,
                    const char *format, ...) {
  va_list args;
  char dbg_buffer[1024];

  va_start(args, format);
  vsnprintf(dbg_buffer, sizeof(dbg_buffer)-1, format, args);
  dbg_buffer[sizeof(dbg_buffer)-1] = 0; 

  CDebug::Out(devName, nLevelMask, dbg_buffer, NULL, 0);
  va_end(args);
}




#ifdef _WINDOWS
  

typedef bool (RSCT_STDCALL *FUNC_SIIMONEX_PROC_OUT)(uint32_t	p_nMsgType,
						    LPCSTR	p_zSource,
						    LPCSTR	p_zText);


static HMODULE module;
static FUNC_SIIMONEX_PROC_OUT debuger;

CDebug::CDebug(unsigned int nLevelMask)
{
 	HKEY hKey;
   unsigned long Result=0;
	module=0;
	m_nLevelMask=0;
	debuger=NULL;
   if(RegOpenKeyEx(HKEY_LOCAL_MACHINE,TEXT("Software\\REINER SCT\\cyberJack Base Components"),0,KEY_READ,&hKey)==ERROR_SUCCESS)
   {
	 	DWORD dwLength;
      DWORD sCTHelp;
		dwLength=sizeof(sCTHelp);
		if(RegQueryValueEx(hKey,TEXT("DebugMask"),NULL,NULL,(LPBYTE)&sCTHelp,&dwLength)==ERROR_SUCCESS)
		{
			Result=sCTHelp;
		}
      RegCloseKey(hKey);
   }
   if(Result)
	{
		m_nLevelMask=Result;
	}
	if((module=LoadLibrary(TEXT("SiiMonEx.dll")))!=NULL)
	{
		debuger=(FUNC_SIIMONEX_PROC_OUT)GetProcAddress(module,TEXT("ProtOut"));
	}
	else
	{
		debuger=NULL;
	}

}

CDebug::~CDebug(void)
{
	if(module)
	{
		debuger=NULL;
		FreeLibrary(module);
		module=NULL;
	}
}

void CDebug::Out(const char *cDeviceName,
                 unsigned int nLevelMask,
                 const char *cCaption,
                 void *pData, uint32_t ulDataLen)
{
	if(m_nLevelMask & nLevelMask)
	{
		if(debuger)
		{
			char filter[32];
			char text[512];
			unsigned int i=0;
			uint8_t *ptr=(uint8_t *)pData;
			char pure_text[17];
			char *pure;

			strcpy(filter,"CJECA32_");
			strncat(filter,cDeviceName,32);
			filter[31]='\0';
			sprintf(text,"%s --- MASK: %08X\n",cCaption,nLevelMask);
			debuger(0,filter,text);
			if (ulDataLen>0)
			{
				sprintf(text,"%11s ","DATA:");
				pure=pure_text;
				memset(pure_text,0,sizeof(pure_text));
				for(i=0;i<ulDataLen;i++)
				{
					sprintf(text+12+(i%16)*3,"%02X ",(int)*ptr);
					if(*ptr>32 && *ptr<128)
						*pure++=*ptr;
					else
						*pure++='.';
					ptr++;

					if((i%16)==15 || i==ulDataLen-1)
					{
						while((i%16)!=15)
						{
							i++;
							sprintf(text+12+(i%16)*3,"   ");
						}
						strcat(text,"--- ");
						strcat(text,pure_text);
						strcat(text,"\n");
						debuger(0,filter,text);
						pure=pure_text;
						memset(pure_text,0,sizeof(pure_text));
						sprintf(text,"%11s ","");
					}
				}
			}
		}
	}
}

CDebug Debug(0xffffffff);



#elif defined(OS_LINUX) || defined(OS_DARWIN)

#if defined(OS_DARWIN)
# define DEBUG_DEFAULT_LOGFILE "/Library/Logs/cj.log"
#else
# define DEBUG_DEFAULT_LOGFILE "/tmp/cj.log"
#endif
#include <time.h>
#include <sys/time.h>



CDebug::CDebug(unsigned int nLevelMask)
:m_nLevelMask(nLevelMask)
,m_logFileName(strdup(DEBUG_DEFAULT_LOGFILE))
{
}



CDebug::~CDebug(void){
  if (m_logFileName)
    free(m_logFileName);
}



void CDebug::setLevelMask(unsigned int nLevelMask) {
  m_nLevelMask=nLevelMask;
}



void CDebug::setLogFileName(const char *fname) {
  if (m_logFileName)
    free(m_logFileName);
  if (fname)
    m_logFileName=strdup(fname);
  else
    m_logFileName=NULL;
}




void CDebug::Out(const char *cDeviceName,
		 unsigned int nLevelMask,
		 const char *cCaption,
		 void *pData,
		 uint32_t ulDataLen){
#if 1
  if (m_nLevelMask & nLevelMask){
    char devName[8];
    const char *mask;
    struct timeval tv;
    struct tm *tm;
    int pid;
    uint32_t i=0;
    uint8_t *ptr=(uint8_t *)pData;
    const char *p;
    FILE *f=NULL;

    if (m_logFileName)
      f=fopen(m_logFileName, "a+");
    if (f==NULL)
      f=stderr;

    /* sample header data */
    i=strlen(cDeviceName);
    if (i>=sizeof(devName)) {
      p=cDeviceName+(i-(sizeof(devName)-1));
      i=sizeof(devName)-1;
    }
    else
      p=cDeviceName;
    strncpy(devName, p, i);
    devName[i]=0;

    switch(nLevelMask) {
    case DEBUG_MASK_COMMUNICATION_OUT:   mask="COMOUT"; break;
    case DEBUG_MASK_COMMUNICATION_IN:    mask="COMIN "; break;
    case DEBUG_MASK_COMMUNICATION_ERROR: mask="COMERR"; break;
    case DEBUG_MASK_COMMUNICATION_INT:   mask="COMINT"; break;
    case DEBUG_MASK_COMMUNICATION_INFO:  mask="COMINF"; break;
    case DEBUG_MASK_TRANSLATION:         mask="TRANS "; break;
    case DEBUG_MASK_RESULTS:             mask="RESULT"; break;
    case DEBUG_MASK_INPUT:               mask="INPUT "; break;
    case DEBUG_MASK_OUTPUT:              mask="OUTPUT"; break;
    case DEBUG_MASK_CTAPI:               mask="CTAPI "; break;
    case DEBUG_MASK_IFD:                 mask="IFD   "; break;
    case DEBUG_MASK_CJECOM:              mask="CJECOM"; break;
    case DEBUG_MASK_PPA:                 mask="PPA   "; break;
    default:                             mask="UNKNWN"; break;
    }

    pid=getpid();
    gettimeofday(&tv, NULL);
    tm=localtime(&tv.tv_sec);

    if (cCaption) {
      i=strlen(cCaption);
      if (i && cCaption[i-1]=='\n')
	fprintf(f, "%s:[%08x]:%04d/%02d/%02d %02d:%02d:%02d:%06d:[%s]:%s",
		mask,
		pid,
		tm->tm_year+1900,
		tm->tm_mon+1,
		tm->tm_mday,
		tm->tm_hour,
		tm->tm_min,
		tm->tm_sec,
		(int)tv.tv_usec,
                devName,
                cCaption);
      else
			fprintf(f, "%s:[%08x]:%04d/%02d/%02d %02d:%02d:%02d:%06d:[%s]:%s\n",
            mask,
	    pid,
	    tm->tm_year+1900,
            tm->tm_mon+1,
            tm->tm_mday,
            tm->tm_hour,
            tm->tm_min,
            tm->tm_sec,
	    (int)tv.tv_usec,
            devName,
            cCaption);
    }
    else
      fprintf(f, "%s:[%08x]:%04d/%02d/%02d %02d:%02d:%02d:%06d:[%s]:(no text)\n",
	      mask,
	      pid,
	      tm->tm_year+1900,
	      tm->tm_mon+1,
	      tm->tm_mday,
	      tm->tm_hour,
	      tm->tm_min,
	      tm->tm_sec,
	      (int)tv.tv_usec,
              devName);
    /* show data if any */
    if (ulDataLen>0 && pData) {
      fprintf(f, "%s:  DATA: ", mask);

      for (i=0; i<ulDataLen; i+=16) {
	int j;

        /* indent */
	if (i)
	  fprintf(f, "%s:        ", mask);

        /* show as hex */
	for (j=0; j<16; j++) {
	  if ((i+j)<ulDataLen)
	    fprintf(f, "%02x ", ptr[i+j]);
	  else
	    fprintf(f, "   ");
	}

        fprintf(f, " - ");

	/* show as text */
	for (j=0; j<16; j++) {
	  if ((i+j)<ulDataLen) {
	    uint8_t c;

	    c=ptr[i+j];
	    if (c<32 || c>126)
              c='.';
	    fprintf(f, "%c", c);
	  }
	  else
            break;
	}

        fprintf(f, "\n");
      }
    }
    if (f!=stderr)
      fclose(f);
  }
#endif
}

CDebug Debug(0);
#endif


/* for all platforms */
extern "C" {

  void rsct_debug_out(const char *cDeviceName, unsigned int nLevelMask,
		      const char *cCaption,
		      void *pData, uint32_t ulDataLen) {
    Debug.Out(cDeviceName, nLevelMask,
	      cCaption,
              pData, ulDataLen);
  }


} /* extern "C" */




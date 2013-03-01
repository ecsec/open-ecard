/***************************************************************************
    begin       : Mon Aug 14 2006
    copyright   : (C) 2006 by Martin Preuss
    email       : martin@libchipcard.de

 ***************************************************************************
 *                                                                         *
 *   This library is free software; you can redistribute it and/or         *
 *   modify it under the terms of the GNU Lesser General Public            *
 *   License as published by the Free Software Foundation; either          *
 *   version 2.1 of the License, or (at your option) any later version.    *
 *                                                                         *
 *   This library is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU     *
 *   Lesser General Public License for more details.                       *
 *                                                                         *
 *   You should have received a copy of the GNU Lesser General Public      *
 *   License along with this library; if not, write to the Free Software   *
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston,                 *
 *   MA  02111-1307  USA                                                   *
 *                                                                         *
 ***************************************************************************/

#ifdef HAVE_CONFIG_H
# include <config.h>
#endif

#include "Platform.h"


#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <ctype.h>

#include "config_p.h"


#define CYBERJACK_CONFIG_GROUP_GENERIC 0
#define CYBERJACK_CONFIG_GROUP_VARS    1
#define CYBERJACK_CONFIG_GROUP_UNKNOWN 99
#define CYBERJACK_CONFIG_FILE "/usr/etc/cyberjack.conf"



static CYBERJACK_CONFIG *_ctapi_config=NULL;



static int _readConfig(FILE *f, CYBERJACK_CONFIG *cfg) {
  int currentGroup=CYBERJACK_CONFIG_GROUP_GENERIC;
  char lbuf[256];

  while(!feof(f)) {
    char *p;
    char *varName;
    char *value;
    unsigned int len;

    lbuf[0]=0;
    if (0==fgets(lbuf, sizeof(lbuf), f)) {
      if (ferror(f)) {
        fprintf(stderr, "CYBERJACK: fgets: %s\n", strerror(errno));
        return -1;
      }
    }

    len=strlen(lbuf);
    if (len<1)
      continue;

    if (lbuf[len-1]=='\n' || lbuf[len-1]=='\r') {
      lbuf[len-1]=0;
      len--;
    }
    if (len<1)
      continue;

    /*fprintf(stderr, "CYBERJACK: Data is \"%s\"\n", lbuf);*/
    p=strchr(lbuf, '#');
    if (p)
      *p=0;
    p=lbuf;
    /* skip blanks */
    while(*p && isspace(*p))
      p++;
    if (!*p)
      continue;

    /* check for group name */
    if (*p=='[') {
      if (strncasecmp(p, "[generic]", 9)==0) {
        currentGroup=CYBERJACK_CONFIG_GROUP_GENERIC;
      }
      else if (strncasecmp(p, "[vars]", 6)==0) {
        currentGroup=CYBERJACK_CONFIG_GROUP_VARS;
      }
      else {
        fprintf(stderr, "CYBERJACK: Unknown group \"%s\", ignoring\n", p);
        currentGroup=CYBERJACK_CONFIG_GROUP_UNKNOWN;
      }
    }
    else {
      /* extract var name */
      varName=p;
      while(*p && !isspace(*p) && *p!='=')
        p++;
      if (!*p)
        /* unexpected end of line */
        continue;
      if (*p!='=') {
        /* found end of var name */
        *p=0;
        p++;
        /* skip blanks */
        while(*p && isspace(*p))
          p++;
      }
      if (*p!='=')
        /* missing equation mark */
        continue;
      *p=0;
      p++;
  
      /* skip blanks */
      while(*p && isspace(*p))
        p++;
      if (!*p)
        /* no value */
        continue;
      value=p;
      p++;
      /* get end of value */
      while(*p && !isspace(*p))
        p++;
      if (*p)
        *p=0;
  
      /* handle key value pair */
      if (currentGroup==CYBERJACK_CONFIG_GROUP_GENERIC) {
        /*fprintf(stderr, "CYBERJACK: Handling [%s]=[%s]\n", varName, value);*/
        if (strcasecmp(varName, "flags")==0) {
          unsigned int j;

          if (1==sscanf(value, "%i", &j)) {
            cfg->flags=j;
          }
          else {
            fprintf(stderr, "CYBERJACK: Bad value for flags: \"%s\"", value);
          }
        }
        else if (strcasecmp(varName, "debugFile")==0) {
          cfg->debugFile=value;
        }
        else if (strcasecmp(varName, "serialFile")==0) {
          cfg->serialFile=value;
        }
        /* add more variables here */
        else {
          fprintf(stderr,
                  "CYBERJACK: Unknown variable \"%s\" in config file\n",
                  value);
        }
      }
      else if (currentGroup==CYBERJACK_CONFIG_GROUP_VARS) {
        cfg->vars.insert(CFG_VARMAP::value_type(varName, value));
      }
    }
  } /* while !feof */

  return 0;
}



static int config_read_file(CYBERJACK_CONFIG *cfg) {
  FILE *f;

  f=fopen(CYBERJACK_CONFIG_FILE, "r");
  if (f==NULL) {
    f=fopen(CYBERJACK_CONFIG_FILE".default", "r");
  }
  if (f) {
    int rv;

    rv=_readConfig(f, cfg);
    fclose(f);
    if (rv)
      return rv;
  }
  return 0;
}



static int config_write_file(CYBERJACK_CONFIG *cfg) {
  FILE *f;

  f=fopen(CYBERJACK_CONFIG_FILE, "w+");
  if (f==NULL) {
    fprintf(stderr,
	    "RSCT: Could not create config file [%s]: %s\n",
	    CYBERJACK_CONFIG_FILE,
	    strerror(errno));
    return -1;
  }

  fprintf(f, "# This file has been automatically created\n");
  fprintf(f, "flags=0x%08x\n", cfg->flags);
  if (!(cfg->debugFile).empty())
    fprintf(f, "debugFile=%s\n", cfg->debugFile.c_str());
  if (!(cfg->serialFile).empty())
    fprintf(f, "serialFile=%s\n", cfg->serialFile.c_str());

  /* possibly write vars */
  if (cfg->vars.size()) {
    CFG_VARMAP::iterator it;

    fprintf(f, "\n[vars]\n");
    for (it=cfg->vars.begin(); it!=cfg->vars.end(); it++) {
      if (it->first.length() && it->second.length()) {
        fprintf(f, "%s=%s\n", it->first.c_str(), it->second.c_str());
      }
    }
  }

  if (fclose(f)) {
    fprintf(stderr,
	    "RSCT: Could not close config file [%s]: %s\n",
	    CYBERJACK_CONFIG_FILE,
	    strerror(errno));
    return -1;
  }

  return 0;
}



int rsct_config_init() {
  /* init CTAPI configuration */
  _ctapi_config=new CYBERJACK_CONFIG;
  if (!_ctapi_config) {
    Debug.Out("none", DEBUG_MASK_CTAPI,
	      "not enough memory available\n", 0, 0);
    return -1;
  }

  _ctapi_config->debugFile="/tmp/cj.log";
  _ctapi_config->flags=CT_FLAGS_DEFAULT;

  if (getenv("CJCTAPI_NO_KEYBEEP"))
    _ctapi_config->flags|=CT_FLAGS_NO_BEEP;
  if (getenv("CJCTAPI_ECOM_KERNEL"))
    _ctapi_config->flags|=CT_FLAGS_ECOM_KERNEL;

  config_read_file(_ctapi_config);

  return 0;
}



void rsct_config_fini() {
  if (_ctapi_config) {
    free(_ctapi_config);
    _ctapi_config=0;
  }
}



int rsct_config_save() {
  if (_ctapi_config)
    return config_write_file(_ctapi_config);
  return 0;
}



unsigned int rsct_config_get_flags() {
  if (!_ctapi_config)
    return 0;
  return _ctapi_config->flags;
}



void rsct_config_set_flags(unsigned int i) {
  if (_ctapi_config)
    _ctapi_config->flags=i;
}



unsigned int rsct_config_get_debug_output_level() {
  /* TODO: set this to 0 here or read from config file */
  return 0xffffffff;
}



const char *rsct_config_get_debug_filename() {
  if (!_ctapi_config)
    return 0;
  if (_ctapi_config->debugFile[0]==0)
    return 0;
  return _ctapi_config->debugFile.c_str();
}



const char *rsct_config_get_serial_filename() {
  if (!_ctapi_config)
    return 0;
  if (_ctapi_config->serialFile[0]==0)
    return 0;
  return _ctapi_config->serialFile.c_str();
}



void rsct_config_set_serial_filename(const char *s) {
  if (_ctapi_config) {
    if (!s)
      _ctapi_config->serialFile="";
    else {
      _ctapi_config->serialFile=s;
    }
  }
}



void rsct_config_set_var(const char *name, const char *val) {
  if (_ctapi_config) {
    if (name && val) {
      _ctapi_config->vars.insert(CFG_VARMAP::value_type(name, val));
    }
  }
}



const char *rsct_config_get_var(const char *name) {
  if (_ctapi_config) {
    if (name) {
      CFG_VARMAP::iterator it;

      it=_ctapi_config->vars.find(name);
      if (it!=_ctapi_config->vars.end())
        return it->second.c_str();
    }
  }

  return NULL;
}








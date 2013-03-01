#ifndef ECA_BASETRANSLATER_H
#define ECA_BASETRANSLATER_H

class CBaseCommunication;


class CBaseTranslater
{
public:
	CBaseTranslater(CBaseCommunication *Communicator);
public:
	virtual ~CBaseTranslater(void);
private:
	CBaseCommunication *m_Communicator;

};
#endif

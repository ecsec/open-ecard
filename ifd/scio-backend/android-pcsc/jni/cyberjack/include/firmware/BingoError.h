#ifndef BINGO_ERROR_H

//Eroor classes
#define ERROR_CARD                        0x10000000  //Class: Card communication errors
#define ERROR_CARD_CMD                    0x20000000  //Class: Card state errors
#define ERROR_USER                        0x30000000  //Class: User errors
#define ERROR_INPUT                       0x40000000  //Class: Input validation errors
#define ERROR_STATE                       0x50000000  //Class: State machine errors


#define ERROR_OPENING_CARD                (ERROR_CARD | 1)
#define ERROR_WRONG_CARD                  (ERROR_CARD | 2)
#define ERROR_COM_CARD                    (ERROR_CARD | 3)
#define ERROR_AUTH_CARD                   (ERROR_CARD | 4)


#define ERROR_CARD_CMD_SELECT(SW1,SW2)              (ERROR_CARD_CMD | 0x00A40000 | (SW1<<8) | SW2)
#define ERROR_CARD_CMD_AUTH(SW1,SW2)                (ERROR_CARD_CMD | 0x00880000 | (SW1<<8) | SW2)
#define ERROR_CARD_CMD_GETCHALLANGE(SW1,SW2)        (ERROR_CARD_CMD | 0x00020000 | (SW1<<8) | SW2)

#define ERROR_USER_TIMEOUT                     (ERROR_USER | 1)
#define ERROR_USER_ABORT                       (ERROR_USER | 2)

#define ERROR_INPUT_LENGTH                     (ERROR_INPUT | 1)
#define ERROR_INPUT_VALUE                      (ERROR_INPUT | 2)

#define ERROR_STATE_NO_RND                     (ERROR_STATE | 1)

#endif

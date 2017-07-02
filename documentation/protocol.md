# IGX Protocol Version 3.8

The IGX Protocol is a simple string-based protocol.  Each value that is sent 
or received contains only printable ASCII characters, and is terminated by a 
newline.

Most lines start with a control character.  This control character need not 
be unique across the entire protocol.

CHECK: On Windows, will the newline be \r\n?
 
1. Server sends protocol version to the connected client("3.8")
2. Client sends back if it accepts the protocol or not("]" means OK, 
 "[" means not OK).  The socket will then be invalid and should be closed. 
 See the section on Protcol Version Control Characters
3. Client sends the alias(username) of the player
4. Server validates the alias, and sents back either \*,\|,{,} depending 
 on how the validation went.  See the section on Alias Control Characters for 
 information on the proper control character.
5. Client sends password(plain text)
6. Server validates the password.  See the section on Password Control 
 Characters for the proper control character response.
7. Server sends text information to the client.  This is a multiple line
 value, ending with a '~' character on a new line.
8. Server sents robot information to the client.  This is a multiple line 
 value, ending with a '~' character on a new line.

## Protocol Version Control Characters
|Character|Meaning|
|---------|-------|
|]|Client accepted protocol version|
|[|Client rejected protocol version|

## Alias Control Characters
|Character|Meaning|
|---------|-------|
|\*|Alias already belongs to robot|
|\||Alias contains illegal characters|
|{|Alias not found, create password|
|}|Enter password for alias|

## Password Control Characters
|Character|Meaning|
|---------|-------|
|]|Incorrect password entered|
|[|Correct password entered|

# Arbitrary-Storage-Protocol-ASP-v1
The protocol supports the storage and retrieval of arbitrary data for the duration of a network session.  Information exchange between the client and server are handled in a one-to-one fashion, i.e. each request by the client is met with a response from the server.  These requests are handled by presenting access methods to the server.  The server will respond with a response code which indicates that the client should proceed, the method was a success, or an error occurred.

The two basic methods GET and PUT store and retrieve those data associated with
a given key.  A type may be associated with each key.  There is no guarantee
that data will survive close of the connection.  The CLEAR method clears a key.
The QUIT method closes the session.


1. Methods


1.1 The PUT Method

The PUT method is the means by which a client requests data of an indicated
length (indicated in decimal integer bytes) be stored by the server under the
supplied key:

   "PUT" [space] [length] [space] [key] [newline]

The server will respond with one of the following codes:

   Proceed, Unknown Error, Length Error, Key Error

Upon receipt of the Proceed code, the client will send its data.  The completion
of this sent data will be indicated by <newline> <period> <newline>, i.e. a
period on its own line.  The server will associate as many bytes of this data as
specified in the method's length.  Superfluous bytes are ignored.  An
under-supply of data will cause a Length Error to result.  A successful store
will result in a Ready code.


1.2 The GET Method

The GET method is the means by which a client requests data associated with the
supplied key to be returned:

   "GET" [space] [length] [space] [key] [newline]

The server will respond with either the requested data and a Ready code or an
error code.  On success, the server immediately begins sending the data
associated with the supplied key up to the given length in bytes.  Upon
completion, it appends a <newline> to the data and sends a Ready code.

In the event of an error, the server may respond with one of the following
codes: 

   Unknown Error, Length Error, Key Error


1.3 The CLEAR Method

The CLEAR method removes a key and its associated data from the server's
consideration.  It is specified as follows:

   "CLEAR" [space] [key] [newline]

And may return one of the following codes:

   Ready, Unknown Error, Key Error


1.4 The QUIT Method

The QUIT method requests that the connection to the server be terminated.  It
results in no response code.


2. Response Codes

Response codes are numeric indicators of direction, information, success, and
failure.  All codes are sent with an immediately following newline.  They are as
follows:


2.1. Ready ("000")

The server is ready to accept the next request.


2.2. Proceed ("001")

The server is ready to receive your information.


2.3. Unknown Error ("100")

An unknown error has occurred.


2.4. Length Error ("101")

The method length is incorrect.  For a PUT method, the server may be unable to
store that amount of data.  For A GET method, the client may have requested more
bytes than were stored with that key.


2.5. Key Error ("102")

There is a problem with the key.  For a PUT method, the key is already present
and thereby in collision.  For a GET or CLEAR method, the key does not exist.


3. Example Sequence

The following is an example session between client and server.  Server sent
information is prepended by S: and client send information by C:

   S: 000
   
   C: PUT 13 foo
   
   S: 001
   
   C: Hello, world!
   
   C: .
   
   S: 000
   
   C: PUT 8 bar
   
   S: 001
   
   C: Rutabaga!
   C: .
   
   S: 000
   
   C: FEED dog
   
   S: 100
   
   C: GET 8 bar
   
   S: Rutabaga
   
   S: 000
   
   C: CLEAR foo
   
   S: 000
   
   C: CLEAR foo
   
   S: 102
   
   C: PUT 8 foo
   
   S: 001
   
   C: I am new data in foo!
   
   C: .
   
   S: 000
   
   C: GET 12 foo
   
   S: 101
   
   C: QUIT
   
4. ABNF Definitions

   <space>   = %x20 ; sp
   
   <newline> = %x0a ; nl
   
   <period>  = %x2e ; .
   
   <key>     = ALPHA / DIGIT
   
   <length>  = DIGIT


5. References

D. Crocker, "Augmented BNF for Syntax Specifications: ABNF", STD 68, RFC 5234,
Brandenburg InternetWorking, January 2008

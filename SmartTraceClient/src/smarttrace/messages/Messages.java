package smarttrace.messages;

import java.util.HashMap;

public class Messages {
	// define variable for events
	public static final int FIRST_PLOT = 0;
	public static final int CONNECT = 1;
	public static final int INVALID_ADDRESS = 2;
	public static final int SEARCH = 3;
	public static final int DEFAULT_EPSILON = 4;
	public static final int LCSS = 5;
	public static final int UPBOUND = 6;
	public static final int ENDSRCH = 7;
	public static final int ENDCONN = 8;
	public static final int OTHER = 9;
	public static final int SERVER_ERR = 10;
	public static final int STATECAL = 11;
	public static final int STATESER = 12;
	public static final int STATERET = 13;
	public static final int QUIT = 14;
	public static final String CRLF = "\r\n";
	public static  HashMap<Integer, String> errors;
}

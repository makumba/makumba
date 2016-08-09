///////////////////////////////
//	 Makumba, Makumba tag library
//	 Copyright (C) 2000-2003 http://www.makumba.org
//
//	 This library is free software; you can redistribute it and/or
//	 modify it under the terms of the GNU Lesser General Public
//	 License as published by the Free Software Foundation; either
//	 version 2.1 of the License, or (at your option) any later version.
//
//	 This library is distributed in the hope that it will be useful,
//	 but WITHOUT ANY WARRANTY; without even the implied warranty of
//	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//	 Lesser General Public License for more details.
//
//	 You should have received a copy of the GNU Lesser General Public
//	 License along with this library; if not, write to the Free Software
//	 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//	 -------------
//	 $Id$
//	 $Name$
/////////////////////////////////////
package org.makumba.util;
import java.io.PushbackInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.InputStream;

/**
 * Class <code>BoundaryInputStream</code> extends <code>InputStream</code>
 * and reads till it finds a boundary in the stream. The bytes after the
 * boundary are put back on the stream.
 *
 * TODO: when there is network congestion is necessary to sleep and read again
 *       an exception is thrown in this implementation
 *
 * @author cristi
 * @version 0.01, 07/06/04
 * @see java.io.PushbackInputStream
 * @since Mak 0.5.10.3
 */
public class BoundaryInputStream extends InputStream {
    
    PushbackInputStream	pis;
    String  boundary;
    boolean boundaryFound = false;
    byte[]  myBuffer;

    public BoundaryInputStream(PushbackInputStream pis, String boundary) {
	this.pis = pis;
	this.boundary = boundary;
	// myBuffer should fit the largest read() ever attempted
	// plus a boundary
	myBuffer = new byte[org.makumba.Text.FILE_LIMIT + boundary.length()];
    }
    
    /**
     * Reads to "buffer" till it finds the boundary given to the constructor
     * It will try to read n + boundary.length() -1 and it will return -1 when the
     * boundary is found.
     */
    public int read(byte[] buffer, int start, int n) throws IOException {

	if (boundaryFound) {
	    return -1;
	}
	
	int bytesRead = pis.read(myBuffer, 0, n + boundary.length() - 1);
	
	if (bytesRead == -1) {
	    throw new IllegalArgumentException("Nothing in the PushbackInputStream buffer to read");
	}

	String content = new String(myBuffer, 0, bytesRead); // TODO: create an indexOf for byte[]
	int index = content.indexOf(boundary);               // instead of converting this to a String

	if (index == -1) 
	    //the first bytesRead bytes in myBuffer doesn't contain the boundary
	    if (bytesRead < boundary.length()) {
		if(myBuffer[0]=='-' && myBuffer[1]=='-' && myBuffer[2]==13 && (bytesRead==3 || bytesRead==4 && myBuffer[3]==10))
		    throw new IllegalArgumentException("End of multipart content");
		    // I'm not sure if Apple Mac have only '10' (LF) instead of CR. The ones with an old Mac OS have LF I think... 
		
		// if it was not the end of the multipart then it can be a
		// network congestion: some more bytes just have to come
		// TODO: need to sleep and read later instead of throwning an Exception
		throw new IOException("Not enough bytes to look for the boundary");

	    } else {
		System.arraycopy((Object) myBuffer, 0, //from, position
				 (Object) buffer, 0, //to, position
				 bytesRead - boundary.length() + 1); //length

		//pushback into pis from bytesRead-boundary.length()+1 to bytesRead
                pis.unread(myBuffer, bytesRead - boundary.length() + 1, boundary.length() - 1);

		return bytesRead - boundary.length() + 1;
	    }
	else { //boundary found, index = beginning of boundary

	    boundaryFound = true;
	    pis.unread(myBuffer, index + boundary.length(),
		       bytesRead - (index + boundary.length()));

	    if (index == 0) //the boundary begins at the 1st byte read
		return -1;

	    // to eliminate the CR and/or LF before the boundary 
	    index-= (myBuffer[index-1]==10)?2:1; // 10 = LF
	    System.arraycopy((Object) myBuffer, 0, //from, position
			     (Object) buffer, 0, //to, position
			     index); 
	    return index;
	}

    }// end method 'read(buffer, start, n)'

    
    byte[]	simpleBuffer	= new byte[1];
    
    /**
     * define read() by calling read(byte[], int, int) not sure if this is
     * needed in the Text(InputStream) constructor, but just to be on the safe
     * side
     */
    public int read() throws IOException {
	return read(simpleBuffer, 0, 1) == -1 ? -1 : simpleBuffer[0];
    }

}// end class BoundaryInputStream

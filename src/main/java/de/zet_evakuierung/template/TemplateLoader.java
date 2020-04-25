/* zet evacuation tool copyright (c) 2007-20 zet evacuation team
 *
 * This program is free software; you can redistribute it and/or
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package de.zet_evakuierung.template;

import org.zetool.common.debug.Debug;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class TemplateLoader extends DefaultHandler {
	/** The logger for the template loader. */
	private final static Logger log = Debug.globalLogger;
	private SAXParser saxParser;
	private TemplateTypes type = null;
	private boolean start = false;
	private Templates<Door> doors = new Templates<>( "doors" );
	private Templates<ExitDoor> exitDoors = new Templates<>( "exitDoors" );
//	private Templates<Delay> delays;
//	private Templates<Stair> stairs;
//	private Templates<Properties> properties;

	public TemplateLoader() throws ParserConfigurationException, SAXException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		saxParser = factory.newSAXParser();
	}

	public void parse( File f ) throws SAXException, IOException {
		saxParser.parse( f, this );
	}

	@Override
	public void startElement( String namespaceURI, String localName, String qName, Attributes atts ) throws SAXException {
		if( start == false && qName.equals( "tmpl" ) ) { // we have not started yet. check if the type is correct.
			for( int i = 0; i < atts.getLength() && start == false; i++ )
				if( atts.getQName( i ).equals( "type" ) )
					for( TemplateTypes t : TemplateTypes.values() )
						if( atts.getValue( i ).equals( t.getXmlCode() ) ) {
							type = t;
							start = true;
							String name = atts.getValue( "name" );
							switch( t ) {
								case Door:
									doors = new Templates<>( name == null ? "doors" : name );
									break;
								case ExitDoor:
									exitDoors = new Templates<>( name == null ? "exitDoors" : name );
							}
							break;
						}
		} else if( start == true && qName.equals( "entry" ) )
			readTemplateEntry( atts );
	}

	private void readTemplateEntry( Attributes atts ) {
		switch( type ) {
			case Door:
				String name = atts.getValue( "name") == null ? "door" : atts.getValue( "name" );
				double priority = Double.parseDouble( atts.getValue( "priority" ) == null ? "1" : atts.getValue( "priority" ) );
				int size = Integer.parseInt( atts.getValue( "size" ) == null ? "800" : atts.getValue( "size" ) );
				doors.add( new Door( name == null ? "door" : name, size, priority ) );
				log.log( Level.FINE, "Door ''{0}'' found with priority ''{1}'' and size ''{2}''", new Object[]{name, priority, size});
				break;
			case ExitDoor:
				name = atts.getValue( "name") == null ? "door" : atts.getValue( "name" );
				priority = Double.parseDouble( atts.getValue( "priority" ) == null ? "1" : atts.getValue( "priority" ) );
				size = Integer.parseInt( atts.getValue( "size" ) == null ? "800" : atts.getValue( "size" ) );
				exitDoors.add( new ExitDoor( name == null ? "door" : name, size, priority ) );
				log.log( Level.FINE, "Door ''{0}'' found with priority ''{1}'' and size ''{2}''", new Object[]{name, priority, size});
				break;
		}
	}

	@Override
	public void endElement( String namespaceURI, String localName, String qName ) {
		if( start == true && qName.equals( "tmpl" ) ) {
			start = false;
			switch( type ) {
				case Door:
					log.log( Level.CONFIG, "Door templates ''{0}'' loaded.", doors.getName());
					break;
				case ExitDoor:
					log.log( Level.CONFIG, "Exit door templates ''{0}'' loaded.", exitDoors.getName());
					break;
			}
		}
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
		saxParser.reset();
	}

	
	
	/**
	 * <p>Returns a list of {@link Door} templates. If no doors have been read so
	 * far, an empty list is returned. If several template files containing
	 * {@link Door} templates have been read, the last set of templates is
	 * returned.</p>
	 * <p>The returned list is the actual list, and the list can be modified.
	 * After a file has been loaded completely, the list is never changed from
	 * inside this class.</p>
	 * @return a list of {@link Door} templates that may be empty
	 */
	public Templates<Door> getDoors() {
		return doors;
	}
	
	public Templates<ExitDoor> getExitDoors() {
		return exitDoors;
	}
	
	public static void main( String args[] ) throws ParserConfigurationException, SAXException, IOException {
		String file = "./templates/door_default.xml";

		Debug.setDefaultLogLevel( Level.ALL );
		Debug.setUpLogging();


		TemplateLoader sax = new TemplateLoader();
		sax.parse( new File( file ) );
	}
}

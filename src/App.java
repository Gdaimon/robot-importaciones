import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class App {
	private static final Logger logger = LoggerFactory.getLogger ( App.class );
	
	public static void main ( String[] args ) {
		
		List < String > numerosAceptacion = new ArrayList <> ( );
		Collections.sort ( numerosAceptacion );
		numerosAceptacion.add ( "482021000253203".trim ( ) );
		
		logger.warn ( "prueba: {}", "Mundo" );
		
	}
}

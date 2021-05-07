package main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class App {
	private static final Logger logger = LoggerFactory.getLogger ( App.class );
	
	public static void main ( String[] args ) {
		
		List < String > numerosAceptacion = Stream.of (
						"192021000022825",
						"032021000396299"
		).map ( String::trim ).collect ( Collectors.toList ( ) );
		
		
		/* Sticker declaraciones de importacion */
		// generarNumerosSticker ( numerosAceptacion );
		
		/* Recibos oficiales de pago */
		// String fechaAceptacion = "07/04/2021";
		// generarReciboOficialpago ( numerosAceptacion, fechaAceptacion );
		
	}
	
	public static void generarNumerosSticker ( List < String > numerosAceptacion ) {
		
		Runtime runtime = Runtime.getRuntime ( );
		int numeroNucleos = runtime.availableProcessors ( );
		ExecutorService pool;
		// Asigno la cantidad de hilos que vamos a disponer para realizar la peticion
		if ( numerosAceptacion.size ( ) > 1 ) {
			pool = Executors.newFixedThreadPool ( numeroNucleos );
		} else {
			pool = Executors.newFixedThreadPool ( 1 );
		}
		
		for ( String numeroAceptacion : numerosAceptacion ) {
			// Creamos Runnables por cada uno de las declaraciones
			pool.submit ( ( ) -> {
				try {
					Utils.generarNumeroSticker ( Utils.iniciarConexionDian ( Utils.obtenerAdministracion ( numeroAceptacion ) ),
									numeroAceptacion,
									Utils.buscarBadaIdDocum ( Utils.iniciarConexionDian ( Utils.obtenerAdministracion ( numeroAceptacion ) ), numeroAceptacion )
					);
				} catch ( Exception e ) {
					e.printStackTrace ( );
				}
			} );
			
		}
		
		// Metodo que se ejecuta hasta que todos las tareas hallan terminado
		pool.shutdown ( );
		try {
			if ( !pool.awaitTermination ( 1000, TimeUnit.MILLISECONDS ) ) {
				pool.shutdownNow ( );
			}
		} catch ( InterruptedException e ) {
			pool.shutdownNow ( );
		}
	}
	
	public static void generarReciboOficialpago ( List < String > numerosAceptacion, String fechaAceptacion ) {
		ExecutorService pool;
		Runtime runtime = Runtime.getRuntime ( );
		int numeroNucleos = runtime.availableProcessors ( );
		// Asigno la cantidad de hilos que vamos a disponer para realizar la peticion
		pool = Executors.newFixedThreadPool ( numeroNucleos );
		
		for ( String numeroAceptacion : numerosAceptacion ) {
			// Creamos Runnables por cada uno de las declaraciones
			pool.submit ( ( ) -> {
				try {
					
					Utils.generarReciboOficialPago (
									Utils.iniciarConexionDian ( Utils.obtenerAdministracion ( numeroAceptacion ) ),
									fechaAceptacion,
									Utils.buscarBadaIdDocum ( Utils.iniciarConexionDian ( Utils.obtenerAdministracion ( numeroAceptacion ) ), numeroAceptacion ),
									numeroAceptacion
					);
					
				} catch ( Exception e ) {
					e.printStackTrace ( );
				}
			} );
			
		}
		
		// Metodo que se ejecuta hasta que todos las tareas hallan terminado
		pool.shutdown ( );
		try {
			if ( !pool.awaitTermination ( 1000, TimeUnit.MILLISECONDS ) ) {
				pool.shutdownNow ( );
			}
		} catch ( InterruptedException e ) {
			pool.shutdownNow ( );
		}
	}
	
	
}

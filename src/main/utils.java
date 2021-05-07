package main;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.PdfMerger;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

class Utils {
	private static final Logger logger = LoggerFactory.getLogger ( Utils.class );
	
	/**
	 * Metodo para realizar la consulta del id de la regional por donde se va a presnetar la declaracion de importacion
	 * @author Carlos Andres Charris S
	 * @since 7/05/2021
	 */
	public static String obtenerAdministracion ( String aceptacion ) {
		String idRegional = aceptacion.substring ( 0, 2 );
		String regional = "";
		switch ( idRegional ) {
			case "01":
				regional = "Armenia";
				break;
			case "87":
				regional = "Barranquilla";
				break;
			case "03":
				regional = "Bogotá";
				break;
			case "04":
				regional = "Bucaramanga";
				break;
			case "35":
				regional = "Buenaventura";
				break;
			case "88":
				regional = "Cali";
				break;
			case "48":
				regional = "Cartagena";
				break;
			case "89":
				regional = "Cúcuta";
				break;
			case "37":
				regional = "Ipiales";
				break;
			case "39":
				regional = "Maicao";
				break;
			case "10":
				regional = "Manizales";
				break;
			case "90":
				regional = "Medellín";
				break;
			case "16":
				regional = "Pereira";
				break;
			case "46":
				regional = "Puerto_Asis";
				break;
			case "25":
				regional = "Riohacha";
				break;
			case "19":
				regional = "Santa Marta";
				break;
			case "41":
				regional = "Urabá";
				break;
		}
		return regional;
	}
	
	/**
	 * Metodo para realizar la consulta a la pagina suministrada
	 * @author Carlos Andres Charris S
	 * @since 7/05/2021
	 */
	public static HttpResponse consultarPaginaPost ( HttpClient httpClient, HttpPost httpPost ) {
		int numero = 10;
		int noIntentos = 0;
		HttpResponse response = null;
		while ( numero > 0 ) {
			try {
				response = httpClient.execute ( httpPost );
			} catch ( Exception e ) {
				numero--;
				noIntentos++;
				logger.info ( "Intentando consultar pagina, intento No.: " + noIntentos + " de 10" );
			}
			
			if ( response != null ) {
				if ( validarCodigoRespuesta ( response.getStatusLine ( ).getStatusCode ( ) ) ) {
					return response;
				} else {
					numero--;
					noIntentos++;
					logger.info ( "Intentando consultar pagina, intento No.: " + noIntentos + " de 10" );
				}
			} else {
				numero--;
				noIntentos++;
				logger.info ( "Intentando consultar pagina, intento No.: " + noIntentos + " de 10" );
			}
			
		}
		return response;
	}
	
	/**
	 * Valida el codigo de respuesta de la pagina
	 * @author Carlos Andres Charris S
	 * @since 7/05/2021
	 */
	static boolean validarCodigoRespuesta ( int codigoRespuesta ) {
		return codigoRespuesta == 200;
	}
	
	/**
	 * Metodo para consultar la declaracion de importacion y consultar su bada_id_docum
	 * @author Carlos Andres Charris S
	 * @since 7/05/2021
	 */
	public static String buscarBadaIdDocum ( HttpClient httpClient, String numeroAceptacion ) {
		List < NameValuePair > parametros = new ArrayList <> ( );
		HttpResponse response;
		HttpEntity entity;
		HttpPost httpPost;
		String mensaje;
		int bada_id_docum = 0;
		String URL = "https://importaciones.dian.gov.co/SIGLOXXI/IMPORTACIONES/m_levante/m_declaracion_importacion/asp/le_02pre_buzon_deim_gen.asp";
		
		try {
			if ( httpClient != null ) {
				// Path consultar y actualizar bada_id_docum
				httpPost = new HttpPost ( URL );
				parametros.add ( new BasicNameValuePair ( "accionX", "IMPR_DECLA" ) );
				parametros.add ( new BasicNameValuePair ( "numero_interno", numeroAceptacion ) );
				parametros.add ( new BasicNameValuePair ( "submit", "CONFIRMAR" ) );
				
				httpPost.setEntity ( new UrlEncodedFormEntity ( parametros, Consts.UTF_8 ) );
				response = consultarPaginaPost ( httpClient, httpPost );
				entity = response.getEntity ( );
				mensaje = EntityUtils.toString ( entity );
				bada_id_docum = obtenerBadaIdDocum ( mensaje );
			} else {
				mensaje = "No se pudo establecer conexion con la DIAN, por favor intente nuevamente";
				System.out.println ( mensaje );
			}
			return Integer.toString ( bada_id_docum );
		} catch ( Exception e ) {
			e.printStackTrace ( );
		}
		return Integer.toString ( bada_id_docum );
	}
	
	/**
	 * Metodo para obtener del html el bada_id_docum
	 * @author Carlos Andres Charris S
	 * @since 7/05/2021
	 */
	public static Integer obtenerBadaIdDocum ( String html ) {
		Document document = Jsoup.parse ( html );
		Elements inputs = document.select ( "input[name=bada_id_docum]" );
		String url = document.select ( "font.Arial05 a[href*=mercancia]" ).attr ( "href" );
		
		URI uri = URI.create ( url );
		Map < String, String > parametros = URLEncodedUtils.parse ( uri, "UTF-8" )
						                                    .stream ( )
						                                    .collect (
										                                    Collectors.toMap ( NameValuePair::getName, NameValuePair::getValue )
						                                    );
		int bada_id_docum = Integer.parseInt ( parametros.get ( "bada_id_docum" ) );
		return Math.max ( bada_id_docum, 0 );
	}
	
	/**
	 * Metodo que realiza la conexion con la DIAN y retorna una session activa de la conexion
	 * @author Carlos Andres Charris S
	 * @since 7/05/2021
	 */
	static HttpClient iniciarConexionDian ( String regional ) {
		HttpClient httpClient = wrapperClientSSL ( new DefaultHttpClient ( ) );
		HttpPost httpPost = null;
		List < NameValuePair > parametros = new ArrayList <> ( );
		HttpResponse response;
		HttpEntity entity;
		final String URL_PRINCIPAL = "https://importaciones.dian.gov.co/sigloxxi/comun/asp/COMEX.asp";
		final String URL_LOGIN = "https://importaciones.dian.gov.co/SIGLOXXI/comun/cm_receptor.asp";
		final String URL_MENU_PRINCIPAL = "https://importaciones.dian.gov.co/SIGLOXXI/comun/menu/menuJava.asp";
		Map < String, String > credenciales = obtenerCredencialesAcceso ( );
		String usuario = credenciales.get ( "user" );
		String password = credenciales.get ( "pass" );
		String mensaje;
		
		try {
			
			// Seteamos la ruta que vamos a consultar
			httpClient.getParams ( ).setParameter ( "http.protocol.cookie-policy", CookiePolicy.BROWSER_COMPATIBILITY );
			final RequestConfig params = configuracionCookieRequest ( );
			
			httpPost = new HttpPost ( URL_PRINCIPAL );
			httpPost.setConfig ( params );
			// Parametros de ingreso pagina principal
			parametros.add ( new BasicNameValuePair ( "admon", codigoAdministracion ( regional ) ) );
			parametros.add ( new BasicNameValuePair ( "apli", "COMEX" ) );
			parametros.add ( new BasicNameValuePair ( "modulo", "IMPORTACIONES" ) );
			parametros.add ( new BasicNameValuePair ( "Entrada", "TRUE" ) );
			parametros.add ( new BasicNameValuePair ( "submit1", "Cargar Modulo" ) );
			// Adicionamos los parametros a la consulta y como es peticion POST se deben codificar los parametros de la consulta
			httpPost.setEntity ( new UrlEncodedFormEntity ( parametros, Consts.UTF_8 ) );
			response = consultarPaginaPost ( httpClient, httpPost );
			entity = response.getEntity ( );
			
			mensaje = EntityUtils.toString ( entity );
			EntityUtils.consume ( entity );
			
			// Verificamos la respueta de la pagina de la DIAN
			if ( response.getStatusLine ( ).getStatusCode ( ) == 200 ) {
				// Path pagina Login
				httpPost = new HttpPost ( URL_LOGIN );
				// Parametros de login usuario
				parametros = new ArrayList <> ( );
				parametros.add ( new BasicNameValuePair ( "id", usuario ) );
				parametros.add ( new BasicNameValuePair ( "password", password ) );
				httpPost.setEntity ( new UrlEncodedFormEntity ( parametros, Consts.UTF_8 ) );
				response = consultarPaginaPost ( httpClient, httpPost );
				entity = response.getEntity ( );
				mensaje = EntityUtils.toString ( entity );
				EntityUtils.consume ( entity );
				
				// Verificamos la respueta de la pagina de la DIAN
				if ( response.getStatusLine ( ).getStatusCode ( ) == 200 ) {
					mensaje = validarIngreso ( mensaje );
					if ( mensaje.equals ( "" ) ) {
						// Path menu principal con login
						httpPost = new HttpPost ( URL_MENU_PRINCIPAL );
						response = consultarPaginaPost ( httpClient, httpPost );
						entity = response.getEntity ( );
						EntityUtils.consume ( entity );
						return httpClient;
					} else {
						logger.error ( "Error en el ingreso de la aplicacion" );
						cerrarConexionDian ( httpClient, httpPost );
					}
				} else {
					logger.error ( "Error en la respuesta del servidor de la DIAN, codigo de respuesta: " + response.getStatusLine ( ).getStatusCode ( ) );
					cerrarConexionDian ( httpClient, httpPost );
				}
			} else {
				logger.error ( "Error en la respuesta del servidor de la DIAN, codigo de respuesta: " + response.getStatusLine ( ).getStatusCode ( ) );
				cerrarConexionDian ( httpClient, httpPost );
			}
		} catch ( Exception error ) {
			cerrarConexionDian ( httpClient, httpPost );
			error.printStackTrace ( );
		}
		return null;
	}
	
	/**
	 * Metodo encargado re realizar el cierre de la conexion de la pagina de la DIAN
	 * @author Carlos Andres Charris S
	 * @since 7/05/2021
	 */
	static void cerrarConexionDian ( HttpClient httpClient, HttpPost httpPost ) {
		if ( httpPost != null ) {
			httpPost.releaseConnection ( );
		}
		if ( httpClient != null ) {
			httpClient.getConnectionManager ( ).shutdown ( );
		}
		
	}
	
	/**
	 * Emboltorio a la clase httpClient para hacer request SSL
	 * @author Carlos Andres Charris S
	 * @since 7/05/2021
	 */
	public static HttpClient wrapperClientSSL ( HttpClient httpClient ) {
		try {
			SSLContext sslContext = SSLContext.getInstance ( "TLS" );
			X509TrustManager trustManager = new X509TrustManager ( ) {
				public void checkClientTrusted ( X509Certificate[] xcs, String string ) throws CertificateException {
				}
				
				public void checkServerTrusted ( X509Certificate[] xcs, String string ) throws CertificateException {
				}
				
				public X509Certificate[] getAcceptedIssuers ( ) {
					return null;
				}
			};
			sslContext.init ( null, new TrustManager[] { trustManager }, null );
			SSLSocketFactory sslSocketFactory = new SSLSocketFactory ( sslContext );
			sslSocketFactory.setHostnameVerifier ( SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER );
			ClientConnectionManager clientConnectionManager = httpClient.getConnectionManager ( );
			SchemeRegistry schemeRegistry = clientConnectionManager.getSchemeRegistry ( );
			schemeRegistry.register ( new Scheme ( "https", sslSocketFactory, 443 ) );
			
			DefaultHttpClient client = new DefaultHttpClient ( clientConnectionManager, httpClient.getParams ( ) );
			client.setRedirectStrategy ( new LaxRedirectStrategy ( ) );
			return client;
		} catch ( Exception ex ) {
			return null;
		}
	}
	
	/**
	 * Metodo para realizar la consulta del id de la regional por donde se va a presnetar la declaracion de importacion
	 * @author Carlos Andres Charris S
	 * @since 7/05/2021
	 */
	public static String codigoAdministracion ( String regional ) {
		String idRegional = "";
		switch ( regional ) {
			case "Armenia":
				idRegional = "01";
				break;
			case "Barranquilla":
				idRegional = "87";
				break;
			case "Bogotá":
				idRegional = "03";
				break;
			case "Bucaramanga":
				idRegional = "04";
				break;
			case "Buenaventura":
				idRegional = "35";
				break;
			case "Cali":
				idRegional = "88";
				break;
			case "Cartagena":
				idRegional = "48";
				break;
			case "Cúcuta":
				idRegional = "89";
				break;
			case "Ipiales":
				idRegional = "37";
				break;
			case "Maicao":
				idRegional = "39";
				break;
			case "Manizales":
				idRegional = "10";
				break;
			case "Medellín":
				idRegional = "90";
				break;
			case "Pereira":
				idRegional = "16";
				break;
			case "Puerto_Asis":
				idRegional = "46";
				break;
			case "Riohacha":
				idRegional = "25";
				break;
			case "Santa Marta":
				idRegional = "19";
				break;
			case "Urabá":
				idRegional = "41";
				break;
		}
		return idRegional;
	}
	
	/**
	 * Metodo que verifica si el login se efectuo correctamente
	 * @author Carlos Andres Charris S
	 * @since 7/05/2021
	 */
	public static String validarIngreso ( String html ) {
		
		Document doc = Jsoup.parse ( html );
		String mensaje = "";
		Elements formas = doc.getElementsByTag ( "script" );
		for ( Element inputElement : formas ) {
			String key = inputElement.toString ( );
			if ( key.length ( ) > 46 ) {
				if ( ( key.substring ( 0, 30 ).compareTo ( "<script language=" + '"' + "JavaScript" + '"' + ">" ) == 0 ) && ( key.indexOf ( "mensaje_FALLO =" ) > 0 ) ) {
					mensaje = key.replace ( "<script language=" + '"' + "JavaScript" + '"' + ">", "" );
					mensaje = mensaje.replace ( "<!-- si no soporta JavaScript oculta el código -->", "" );
					mensaje = mensaje.replace ( "mensaje_FALLO =", "" );
					mensaje = mensaje.replace ( "alert(mensaje_FALLO);", "" );
					mensaje = mensaje.replace ( "// End script hiding -->", "" );
					mensaje = mensaje.replace ( "</script>", "" );
					mensaje = mensaje.replace ( "\t", "" );
					mensaje = mensaje.replace ( "\\n", " " );
					mensaje = mensaje.replace ( "\\t", "" );
				}
			}
		}
		return mensaje;
	}
	
	/**
	 * Metodo para setear las cookies basicas del request
	 * @author Carlos Andres Charris S
	 * @since 7/05/2021
	 */
	public static RequestConfig configuracionCookieRequest ( ) {
		return RequestConfig
						       .custom ( )
						       .setCookieSpec ( CookieSpecs.STANDARD )
						       .build ( );
	}
	
	static void generarNumeroSticker ( HttpClient httpClient, String numeroAceptacion, String badaIdDocum ) {
		List < NameValuePair > parametros = new ArrayList <> ( );
		HttpResponse response;
		HttpPost httpPost;
		HttpEntity entity;
		String mensaje;
		
		String fechaAcepta = "";
		// Antigua URL
		// String URL = "https://importaciones.dian.gov.co/SIGLOXXI/IMPORTACIONES/m_levante/m_documentos_soporte/asp/le_02pre_deim_ceros.asp?bada_id_docum=" + badaIdDocument + "&n_acepta=" + numeroAcepta + "&f_acepta=" + fechaAcepta;
		String URL = "https://importaciones.dian.gov.co/SIGLOXXI/IMPORTACIONES/m_levante/m_documentos_soporte/asp/le_02pre_deim_ceros.asp";
		try {
			// Seteamos la ruta que vamos a consultar
			httpPost = new HttpPost ( URL );
			// Adicinamos los parametros de la consulta
			parametros.add ( new BasicNameValuePair ( "bada_id_docum", badaIdDocum ) );
			parametros.add ( new BasicNameValuePair ( "n_acepta", numeroAceptacion ) );
			parametros.add ( new BasicNameValuePair ( "f_acepta", fechaAcepta ) );
			
			httpPost.setEntity ( new UrlEncodedFormEntity ( parametros, Consts.UTF_8 ) );
			response = consultarPaginaPost ( httpClient, httpPost );
			
			entity = response.getEntity ( );
			// Consumo de la respuesta de la pagina
			mensaje = EntityUtils.toString ( entity );
			
			Document document = Jsoup.parse ( mensaje );
			
			String noSticker = document.select ( "table[border=0] tr > td:contains(Auto-Adhesivo) > font > b" )
							                   .first ( )
							                   .parent ( )
							                   .parent ( )
							                   .parent ( )
							                   .select ( "td[align=left] > font > b" )
							                   .text ( );
			
			System.out.println ( "---------------- Datos declaracion ----------------" );
			System.out.println ( "Declaracion No. : " + numeroAceptacion );
			System.out.println ( "Sticker No. : " + noSticker );
			System.out.println ( "------------------------   ------------------------" );
			
		} catch ( Exception e ) {
			e.printStackTrace ( );
		}
	}
	
	static Map < String, String > obtenerCredencialesAcceso ( ) {
		try ( InputStream inputStream = new FileInputStream ( "src/main/resources/config.properties" ) ) {
			
			if ( inputStream == null ) {
				System.out.println ( "No se encontro el archivo config.properties" );
				return new HashMap <> ( );
			}
			Properties properties = new Properties ( );
			// cargamos los valores del inputStream al objero properties
			properties.load ( inputStream );
			Map < String, String > credenciales = new HashMap <> ( );
			credenciales.put ( "user", properties.getProperty ( "user" ) );
			credenciales.put ( "pass", properties.getProperty ( "pass" ) );
			return credenciales;
		} catch ( IOException ex ) {
			ex.printStackTrace ( );
			return new HashMap <> ( );
		}
	}
	
	public static void generarReciboOficialPago ( HttpClient httpClient, String fechaAceptacion, String badaIdDocum, String numeroAceptacion ) {
		List < NameValuePair > parametros = new ArrayList < NameValuePair > ( );
		HttpResponse response = null;
		HttpPost httpPost = null;
		HttpEntity entity = null;
		String mensaje = "";
		String fechaDeclaracion = LocalDate.parse ( fechaAceptacion, DateTimeFormatter.ofPattern ( "d/MM/yyyy" ) ).format ( DateTimeFormatter.ofPattern ( "yyyy-MM-d" ) );
		String URL = "https://importaciones.dian.gov.co/SIGLOXXI/IMPORTACIONES/m_levante/m_documentos_soporte/asp/pago_electronico.asp";
		
		try {
			// Seteamos la ruta que vamos a consultar
			httpPost = new HttpPost ( URL );
			// Adicinamos los parametros de la consulta
			
			parametros = new ArrayList < NameValuePair > ( ); //"2019-05-16";
			parametros.add ( new BasicNameValuePair ( "fecha_entrega", fechaDeclaracion ) );
			parametros.add ( new BasicNameValuePair ( "bada_id_docum", badaIdDocum ) );
			parametros.add ( new BasicNameValuePair ( "submit", "INGRESAR" ) );
			// Adicionamos los parametros a la consulta y como es peticion POST se deben codificar los parametros de la consulta
			httpPost.setEntity ( new UrlEncodedFormEntity ( parametros, Consts.UTF_8 ) );
			
			response = consultarPaginaPost ( httpClient, httpPost );
			entity = response.getEntity ( );
			// Consumo de la respuesta de la pagina
			mensaje = EntityUtils.toString ( entity );
			System.out.println ( mensaje );
			
			// Crear la ruta donde guardaremos los archivos
			String rutaDirectorio = rutaCarpetaRecibos ( );
			Path path = Paths.get ( rutaDirectorio + numeroAceptacion + ".html" );
			Files.write ( path, mensaje.getBytes ( ) );
			
			PdfWriter writer = new PdfWriter ( rutaDirectorio + numeroAceptacion + ".pdf" );
			PdfDocument pdf = new PdfDocument ( writer );
			PdfMerger merger = new PdfMerger ( pdf );
			ConverterProperties properties = new ConverterProperties ( );
			
			String urlImagen = "https://importaciones.dian.gov.co/SIGLOXXI/IMPORTACIONES/imagenes/logo-dian.jpg";
			Document documentHtml = Jsoup.parse ( mensaje );
			Element imagen = documentHtml.selectFirst ( "img" );
			imagen.attr ( "src", urlImagen );
			
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream ( );
			PdfDocument paginaTemporalPdf = new PdfDocument ( new PdfWriter ( byteArrayOutputStream ) );
			HtmlConverter.convertToPdf ( new ByteArrayInputStream ( mensaje.getBytes ( StandardCharsets.UTF_8 ) ), paginaTemporalPdf, properties );
			paginaTemporalPdf = new PdfDocument ( new PdfReader ( new ByteArrayInputStream ( byteArrayOutputStream.toByteArray ( ) ) ) );
			merger.merge ( paginaTemporalPdf, 1, paginaTemporalPdf.getNumberOfPages ( ) );
			paginaTemporalPdf.close ( );
			pdf.close ( );
			
			
		} catch ( Exception e ) {
			e.printStackTrace ( );
		}
	}
	
	static String rutaCarpetaRecibos ( ) {
		try {
			Path path = Paths.get ( System.getProperty ( "user.home" ) + "\\Desktop\\recibos" );
			File directory = new File ( path.toString ( ) );
			
			if ( !directory.exists ( ) ) {
				Files.createDirectories ( path );
			}
			System.out.println ( "Se guardo el archivo en la ruta:" );
			System.out.println ( path );
			return path.toString ( );
		} catch ( Exception e ) {
			e.printStackTrace ( );
			return "";
		}
	}
	
}

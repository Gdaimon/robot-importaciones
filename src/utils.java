public class utils {
	
	/**
	 * Metodo para realizar la consulta del id de la regional por donde se va a presnetar la declaracion de importacion
	 * @param aceptacion
	 * @return
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
}

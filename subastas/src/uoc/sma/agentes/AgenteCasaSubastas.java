package uoc.sma.agentes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.logging.FileHandler;
import uoc.sma.behaviours.CasaSubastasBusquedasBehaviourResponder;
import uoc.sma.behaviours.CasaSubastasTransaccionesBehaviourResponder;
import uoc.sma.datos.Subasta;
import uoc.sma.datos.Transaccion;
import uoc.sma.estrategias.ApuestaUnicaIncrementoDinamicoCfgImpl;
import uoc.sma.estrategias.ApuestaUnicaNRondasCfgImpl;
import uoc.sma.estrategias.ICfgSubasta;
import uoc.sma.estrategias.IncrementalCfgImpl;
import uoc.sma.estrategias.SubastaHolandesaImpl;
import uoc.sma.estrategias.SubastaInglesaImpl;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;
import jade.util.leap.HashMap;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;
import jade.wrapper.StaleProxyException;

/**
 * Agente con las responsabilidades siguientes: �]Crear un agente subastador
 * cuando se crea una nueva subasta �]Crear la subasta �]Obtener la lista de
 * bienes que se subastan. Para acotar el problema se considera que no puede
 * haber 2 subastas activas que subasten el mismo bien. �]Buscar el ID de la
 * subasta segun el bien subastado �]Eliminar al agente subastador cuando
 * concluye la subasta
 *
 * Comportamientos: �]Atender las peticiones de escritura en el historial de
 * transacciones �]Atender las peticiones de busqueda de subastas
 *
 * @author Rodolfo de Benito Arango
 *
 */
@SuppressWarnings("serial")
public class AgenteCasaSubastas extends AgenteBase {
	private HashMap subastas; // La lista de subastas activas (id, subasta)
	private ArrayList<Transaccion> historialTransacciones; // El historial de
															// transacciones
	private HashMap bienesSubastados; // La lista de bienes subastados
										// (bienSubastado, idSubasta)
	private PlatformController contenedor; // Una referencia al contenedor de
											// agentes
	private final Logger logger = Logger.getMyLogger(this.getClass().getName());

	@Override
	/**
	 * Al inicializar el agente realiza las operaciones siguientes:
	 -58/106-
	 * �]Inicializar las listas de historial de transacciones
	 * �]Inicializar la lista de subastas activas
	 * �]Inicializar la lista de bienes subastados
	 * �]Anadir los comportamientos al agente
	 * �]Registrarse en las paginas amarillas
	 *
	 */
	protected void setup() {
		super.setup();
		// Habilita el logger para escribir en el fichero Historial.log
		try {
			FileHandler handler;
			handler = new FileHandler("Historial.log");
			logger.addHandler(handler);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.setLevel(Logger.INFO);
		this.contenedor = getContainerController(); // Obtiene una referencia al
													// contenedor
		// Inicializacion de estructuras de datos
		inicializarEstructurasDatos();
		// Registro en paginas amarillas
		registrarDFAgente("CasaDeSubastas", getLocalName()
				+ "�]Casa de subastas");
		addHistorialTransaccionesBehaviour();
		addBusquedaSubastasBehaviour();
		// A modo de prueba anade subastas al sistema
		testSubastas();
	}
	
	

	/**
	 * Anade el comportamiento que atiende a las peticiones de busquedas de
	 * subastas
	 *
	 * Sigue el protocolo FIPA_QUERY
	 */
	private void addBusquedaSubastasBehaviour() {
		logger.log(Logger.FINE, "Agente " + getLocalName()
				+ " esperando peticiones de busqueda de subastas...");
		MessageTemplate template = MessageTemplate.and(MessageTemplate
				.MatchProtocol(FIPANames.InteractionProtocol.FIPA_QUERY),
				MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF));
		addBehaviour(new CasaSubastasBusquedasBehaviourResponder(this, template));
	}

	/**
	 * Anade el comportamiento que se encarga de atender las peticiones de
	 * anadir informacion de la subasta al historial
	 *
	 * Sigue el protocolo FIPA�]REQUEST
	 */
	private void addHistorialTransaccionesBehaviour() {
		logger.log(Logger.FINE, "Agente " + getLocalName()
				+ " esperando peticiones para el historial de transacciones...");
		MessageTemplate protocolo = MessageTemplate
				.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		MessageTemplate performativa = MessageTemplate
				.MatchPerformative(ACLMessage.REQUEST);
		MessageTemplate plantilla = MessageTemplate
				.and(protocolo, performativa);

		addBehaviour(new CasaSubastasTransaccionesBehaviourResponder(this,
				plantilla));
	}

	/**
	 * Inicializa las estructuras de datos necesarias para el agente
	 */
	private void inicializarEstructurasDatos() {
		historialTransacciones = new ArrayList<Transaccion>();
		subastas = new HashMap();
		bienesSubastados = new HashMap();
	}

	/**
	 * Agrega una transaccion al historial
	 *
	 * @param t
	 *            La transaccion a agregar
	 */
	public void addTransaccion(Transaccion t) {
		historialTransacciones.add(t);
		logger.log(Logger.FINE, "Se ha anadido la transaccion " + t);
		if (t.getOperacion().equalsIgnoreCase(Transaccion.FIN)) {
			logger.log(Logger.FINE, "Se cierra la subasta " + t.getIdSubasta());
			this.cerrarSubasta(t.getIdSubasta());
		}
	}

	/**
	 * Obtiene el historial de transacciones
	 *
	 * @return La lista de transacciones
	 */
	public ArrayList<Transaccion> getTransacciones() {
		return historialTransacciones;
	}

	/**
	 * Anade una nueva subasta al sistema �]Crea el Agente vendedor y lo
	 * asocia a la subasta �]Anade la subasta a la lista de subastas activas
	 * �]Anade el bien subastado a la lista de bienes �]Crea una nueva
	 * transaccion de inicio de subasta y la anade al historial
	 *
	 * @param s
	 *            �] La subasta a anadir
	 * @param estrategiaSubasta
	 *            La estrategia de subasta del vendedor (subasta inglesa,
	 *            holandesa, etc.)
	 */
	private void addSubasta(Subasta s, ICfgSubasta estrategiaSubasta) {
		crearAgenteSubastador(s, estrategiaSubasta);
		subastas.put(s.getIdSubasta(), s);
		addBienSubastado(s.getObjetoSubastado(), s.getIdSubasta());
		addTransaccion(new Transaccion(s.getFechaInicio(), s.getIdSubasta(), s
				.getAgenteSubastador().getLocalName(), "INICIO", s.getPrecio()));
		logger.log(Logger.FINE, "Se ha creado en el sistema la subasta " + s);
	}

	/**
	 * Elimina la subasta de la lista de subastas activas
	 *
	 * @param id
	 *            �] El identificador de la subasta a eliminar
	 */
	public void removeSubasta(int id) {
		subastas.remove(id);
	}

	/**
	 * Obtiene la subasta indicada por el identificador
	 *
	 * @param id
	 *            El identidficador de la subasta
	 * @return La subasta
	 */
	public Subasta getSubasta(int id) {
		return (Subasta) subastas.get(id);
	}

	/**
	 * Anade un nuevo bien subastado a la lista
	 *
	 * Cada bien diferente se identifica en un HashMap cuyas claves referencian
	 * a un ArrayList de subastas para permitir colisiones en el HashMap
	 *
	 * @param b
	 *            �] El bien que se subasta
	 * @param l
	 */
	public void addBienSubastado(String b, int l) {
		bienesSubastados.put(b.toUpperCase(), new Integer(l));
		logger.log(Logger.FINE, "Hay un nuevo bien subastado " + b
				+ " en la subasta " + l);
	}

	/**
	 * Elimina un bien de la lista de bienes subastados
	 *
	 * @param b
	 *            El bien a eliminar
	 */
	public void removeBienSubastado(String b) {
		bienesSubastados.remove(b.toUpperCase());
	}

	/**
	 * Localiza la subasta que contiene el bien a buscar Si no lo encuentra
	 * devuelve 0
	 *
	 * @param bienSubastado
	 *            El bien a localizar en la subasta
	 * @return La subasta
	 */
	public Subasta getSubasta(String bienSubastado) {
		if (bienesSubastados != null) {
			if (bienesSubastados.get(bienSubastado.toUpperCase()) != null) {
				return (Subasta) subastas.get((Integer) bienesSubastados
						.get(bienSubastado.toUpperCase()));
			}
		}
		return null;
	}

	/**
	 * Devuelve toda la lista de subastas
	 * 
	 * @return
	 */
	public Iterator getAllSubastas() {
		return (Iterator) this.subastas.values();
	}

	/**
	 * Crea un nuevo agente subastador encargado de una subasta
	 *
	 * @param s
	 *            La subasta que atiende el agente vendedor
	 * @param estrategiaSubasta
	 *            La estrategia que seguira la subasta (Inglesa, Holandesa, etc)
	 */
	private void crearAgenteSubastador(Subasta s, ICfgSubasta estrategiaSubasta) {
		Object[] args = new Object[2];
		args[0] = s;
		args[1] = estrategiaSubasta;
		try {
			contenedor.createNewAgent("Subastador " + s.getIdSubasta(),
					"uoc.sma.agentes.AgenteSubastador", args).start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		} catch (ControllerException e) {
			e.printStackTrace();
		}
		s.setAgenteSubastador(new AID("Subastador " + s.getIdSubasta(),
				AID.ISLOCALNAME));
		logger.log(Logger.FINE,
				"El agente de casa de subastas crea el agente Subastador para la subasta"
						+ s.getIdSubasta());
	}

	/**
	 * Elimina de la memoria el agente subastador
	 *
	 * @param localname
	 *            El agente a eliminar
	 */
	private void eliminarAgenteSubastador(String localname) {
		try {
			contenedor.getAgent(localname).kill();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		} catch (ControllerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Realiza las operaciones necesarias para eliminar la subasta una vez esta
	 * haya concluido: �]Quitar la subasta de la lista �]Quitar el bien
	 * subastado de la lista �]Eliminar al agente subastador responsable de la
	 * subasta
	 *
	 * @param idSubasta
	 *            El identificador de la subasta a cerrar
	 */
	public void cerrarSubasta(int idSubasta) {
		removeBienSubastado(getSubasta(idSubasta).getObjetoSubastado());
		eliminarAgenteSubastador(getSubasta(idSubasta).getAgenteSubastador()
				.getLocalName());
		removeSubasta(idSubasta);
	}

	@Override
	/**
	 * Al finalizar el agente:
	 * �] Quitar el registro de las paginas amarillas
	 */
	protected void takeDown() {
		super.takeDown();
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {

			fe.printStackTrace();
		}
		logger.log(Logger.INFO, "Historial de transacciones:"
				+ this.historialTransacciones);
	}

	/**
	 * Metodo que comprueba el funcionamiento de las subastas inglesa y
	 * holandesa Crea una subasta inglesa y una subasta holandesa
	 *
	 * Crea 3 agentes compradores para una subasta inglesa y 3 compradores para
	 * una subasta holandesa.
	 *
	 * Crea un agente buscador.
	 *
	 */
	private void testSubastas() {
		GregorianCalendar fInicioS1 = new GregorianCalendar();
		fInicioS1.add(GregorianCalendar.MINUTE, 1);
		Subasta s1 = new Subasta(1, fInicioS1.getTime(), "iMac 25", 2.25, 50,
				true, Subasta.SUBASTA_INGLESA);
		// ICfgSubasta e1 = new SubastaInglesaImpl();
		GregorianCalendar fInicioS2 = new GregorianCalendar();
		fInicioS2.add(GregorianCalendar.MINUTE, 2);
		Subasta s2 = new Subasta(2, fInicioS2.getTime(), "Mac Book pro", 250,
				0, true, Subasta.SUBASTA_HOLANDESA);
		// Crea 2 subasta de ejemplo
		addSubasta(s1, new SubastaInglesaImpl());
		addSubasta(s2, new SubastaHolandesaImpl());
		try {
			// Crea agente buscador de subastas
			contenedor.createNewAgent("Buscador",
					"uoc.sma.agentes.AgenteBuscador", null).start();
			logger.log(Logger.INFO,
					"El agente de casa de subastas crea el agente Buscador");
			testCrearCompradoresInglesa(s1);
			testCrearCompradoresHolandesa(s2);
		} catch (StaleProxyException e) {
			e.printStackTrace();
		} catch (ControllerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Crea 3 compradores de prueba para la subasta Holandesa
	 *
	 * @param s2
	 * @throws StaleProxyException
	 * @throws ControllerException
	 */
	private void testCrearCompradoresHolandesa(Subasta s2)
			throws StaleProxyException, ControllerException {
		Object[] args4 = new Object[2];
		args4[0] = s2;
		args4[1] = new ApuestaUnicaIncrementoDinamicoCfgImpl(20, 100, 160, 4);
		Object[] args5 = new Object[2];
		args5[0] = s2;
		args5[1] = new ApuestaUnicaIncrementoDinamicoCfgImpl(10, 120, 180, 4);
		Object[] args6 = new Object[2];
		args6[0] = s2;

		args6[1] = new ApuestaUnicaNRondasCfgImpl(0, 140, 180, 5);
		contenedor.createNewAgent("Comprador4",
				"uoc.sma.agentes.AgenteComprador", args4).start();
		logger.log(Logger.FINE,
				"El agente de casa de subastas crea el agente Comprador4 para la subasta"
						+ s2.getIdSubasta());
		contenedor.createNewAgent("Comprador5",
				"uoc.sma.agentes.AgenteComprador", args5).start();
		logger.log(Logger.FINE,
				"El agente de casa de subastas crea el agente Comprador5 para la subasta"
						+ s2.getIdSubasta());
		contenedor.createNewAgent("Comprador6",
				"uoc.sma.agentes.AgenteComprador", args6).start();
		logger.log(Logger.FINE,
				"El agente de casa de subastas crea el agente Comprador6 para la subasta"
						+ s2.getIdSubasta());
	}

	/**
	 * Crea compradores de prueba para la subasta Inglesa
	 *
	 * @param s1
	 * @throws StaleProxyException
	 * @throws ControllerException
	 */
	private void testCrearCompradoresInglesa(Subasta s1)
			throws StaleProxyException, ControllerException {
		Object[] args1 = new Object[2];
		args1[0] = s1;
		args1[1] = new IncrementalCfgImpl(0, 10, 25, 190);
		Object[] args2 = new Object[2];
		args2[0] = s1;
		args2[1] = new IncrementalCfgImpl(1, 2, 5, 195);
		Object[] args3 = new Object[2];
		args3[0] = s1;
		args3[1] = new IncrementalCfgImpl(1, 15, 30, 175);
		contenedor.createNewAgent("Comprador1",
				"uoc.sma.agentes.AgenteComprador", args1).start();
		logger.log(Logger.FINE,
				"El agente de casa de subastas crea el agente Comprador1 para la subasta"
						+ s1.getIdSubasta());
		contenedor.createNewAgent("Comprador2",
				"uoc.sma.agentes.AgenteComprador", args2).start();
		logger.log(Logger.FINE,
				"El agente de casa de subastas crea el agente Comprador2 para la subasta"
						+ s1.getIdSubasta());
		contenedor.createNewAgent("Comprador3",
				"uoc.sma.agentes.AgenteComprador", args3).start();
		logger.log(Logger.FINE,
				"El agente de casa de subastas crea el agente Comprador3 para la subasta"
						+ s1.getIdSubasta());
	}
}



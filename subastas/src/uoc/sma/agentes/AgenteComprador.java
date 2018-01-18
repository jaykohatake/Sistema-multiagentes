package uoc.sma.agentes;

import uoc.sma.behaviours.CompradorSuscribirBehaviourInitiator;
import uoc.sma.behaviours.estrategias.CompradorHolandesaIncrementoDinamicoBehaviour;
import uoc.sma.behaviours.estrategias.CompradorHolandesaNRondasBehaviour;
import uoc.sma.behaviours.estrategias.CompradorInglesaIncrementalBehaviour;
import uoc.sma.datos.Subasta;
import uoc.sma.estrategias.ApuestaUnicaIncrementoDinamicoCfgImpl;
import uoc.sma.estrategias.ApuestaUnicaNRondasCfgImpl;
import uoc.sma.estrategias.ICfgApuesta;
import uoc.sma.estrategias.IncrementalCfgImpl;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.util.Logger;

/**
 * Agente responsable de: �]Pujar en una subasta acorde a unos parametros
 * fijados por el usuario �]Admite varias estrategias: Incremental: partiendo
 * de un precio minimo y uno maximo, el agente realiza pujas -69/106- a un
 * intervalo de tiempos fijado por el usuario incrementando la puja en una
 * cantidad fija estipulada por el usuario. Francotirador: parte de un precio
 * maximo al que esta dispuesto a llegar el comprador (precio de reserva) y
 * realiza un puja en el ultimo minuto de la subasta por una importe comprendido
 * entre la puja maxima en ese momento y el precio de reserva del comprador.
 * Primerapuja: el agente hace una puja instantanea por el importe senalado por
 * el comprador.
 *
 * El sistema esta abierto a implementar cualquier otra estrategia sin que esto
 * implique cambio alguno en la arquitectura del SMA.
 *
 * @author Rodolfo de Benito
 *
 */
@SuppressWarnings("serial")
public class AgenteComprador extends AgenteBase {
	private final Logger logger = Logger.getMyLogger(this.getClass().getName());
	private Subasta subasta; // Contiene una referencia a la subasta a la que se
								// suscribira el comprador
	private ICfgApuesta estrategia;
	private AID idBuscador = null;

	public ICfgApuesta getEstrategia() {
		return estrategia;
	}

	public void setEstrategia(ICfgApuesta estrategiaComprador) {
		this.estrategia = estrategiaComprador;
	}

	public Subasta getSubasta() {
		return subasta;
	}

	public void setSubasta(Subasta subasta) {
		this.subasta = subasta;
	}

	@Override
	protected void setup() {
		logger.setLevel(Logger.INFO);
		// Comprueba que el numero de argumentos pasados en la creacion del
		// agente sean correctos
		if ((getArguments() != null) && (getArguments().length >= 2)) {
			setEstrategia((ICfgApuesta) getArguments()[1]);
		} else {
			logger.log(Logger.FINE, "El agente " + this.getLocalName()
					+ " no dispone de ninguna estrategia");
			this.doDelete();
		}
		// Localiza al Agente Buscador en las paginas amarillas
		DFAgentDescription[] aBuscador = getAgentePaginasAmarillas("BUSCADOR");
		if (aBuscador != null)
			this.idBuscador = aBuscador[0].getName();
		addSuscripcionBehaviour();
		addEstrategiaBehaviour();
		addBusquedaBehaviour();
		testAgenteBuscador("iMac 25");
	}

	/**
	 * Metodo de prueba que utiliza al aagente buscador para localizar subastas
	 * activas que contenga el texto pasado como parametro
	 *
	 * @param cadenaBusqueda
	 */
	private void testAgenteBuscador(String cadenaBusqueda) {
		if (idBuscador != null) {
			logger.log(Logger.FINE, this.getLocalName()
					+ " verificando el funcionamiento del agente buscador");
			ACLMessage mensaje = new ACLMessage(ACLMessage.REQUEST);
			mensaje.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
			mensaje.setContent(cadenaBusqueda);
			mensaje.addReceiver(idBuscador);
			this.send(mensaje);
		} else
			logger.log(
					Logger.INFO,
					this.getLocalName()
							+ " no se pudo localizar al agente buscador en paginas amarillas");
	}

	/**
	 * Anade como comportamiento una clase interna que, a su vez, anadira como
	 * comportamiento la recepcion de un mensaje de tipo INFORM procedente del
	 * agente buscador
	 */
	private void addBusquedaBehaviour() {
		addBehaviour(new Behaviour() {
			// Plantilla para responder a los mensajes tipo INFORM procedentes
			// del agente buscador
			MessageTemplate template = MessageTemplate.and(MessageTemplate
					.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));

			public void action() {
				// Espera un mensaje tipo INFORM
				ACLMessage mensaje = receive(template);
				if (mensaje != null) {
					try {
						if (mensaje.getContentObject() != null) {
							// Muestra la subasta buscada
							logger.log(
									Logger.INFO,
									"Agente buscador: subasta localizada "
											+ (Subasta) mensaje
													.getContentObject());
						} else
							logger.log(Logger.INFO,
									" El agente buscador no ha localizado ninguna subasta");
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				} else {
					block();
				}
			}

			@Override
			public boolean done() {
				return false;
			}
		});
	}

	/**
	 * Anade como comportamiento una clase interna que, a su vez, anadira como
	 * comportamiento la estrategia que corresponda al recibir un mensaje de
	 * tipo CFP (simboliza el inicio de la subasta)
	 */
	private void addEstrategiaBehaviour() {
		addBehaviour(new Behaviour() {
			// Plantilla para responder a los mensajes tipo CFP
			private MessageTemplate mt = MessageTemplate
					.MatchPerformative(ACLMessage.CFP);

			public void action() {
				// Espera un mensaje tipo CFP
				ACLMessage cfp = receive(mt);
				if (cfp != null) {
					// Cuando recibe el mensaje anade el comportamiento que
					// coresponde a la estrategia de compra
					logger.log(Logger.FINE, myAgent.getLocalName()
							+ " : RECIBE : CFP");
					if (estrategia instanceof ApuestaUnicaIncrementoDinamicoCfgImpl) {
						myAgent.addBehaviour(new CompradorHolandesaIncrementoDinamicoBehaviour(
								myAgent, cfp));
					}
					if (estrategia instanceof ApuestaUnicaNRondasCfgImpl) {
						myAgent.addBehaviour(new CompradorHolandesaNRondasBehaviour(
								myAgent, cfp));
					}
					if (estrategia instanceof IncrementalCfgImpl) {
						myAgent.addBehaviour(new CompradorInglesaIncrementalBehaviour(
								myAgent, cfp));
					}
				} else {
					block();
				}
			}

			@Override
			public boolean done() {
				return false;
			}
		});
	}

	/**
	 * Anade el comportamiento para que el comprador pueda suscribirse a una
	 * subasta cuyo ID se pasa como parametro en la creacion del agente
	 *
	 * Sigue el protocolo FIPA�]Subscribe
	 */
	private void addSuscripcionBehaviour() {
		// Se crea un mensaje de tipo SUBSCRIBE y se asocia al protocolo
		// FIPA�]Subscribe.
		ACLMessage mensaje = new ACLMessage(ACLMessage.SUBSCRIBE);
		mensaje.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
		// Trabaja con el primero de los argumentos de creacion del agente
		// para obtener el identificador de la subasta al que va a suscribirse
		// el agente comprador
		if (getArguments() != null) {
			setSubasta((Subasta) getArguments()[0]);
			int idSubasta = subasta.getIdSubasta();
			// Pasa en el mensaje el identificador de la subasta a la que debe
			// suscribirse
			mensaje.setContent(String.valueOf(idSubasta)); // La subasta a la
															// que se suscribe
			// Anade el comportamiento
			this.addBehaviour(new CompradorSuscribirBehaviourInitiator(this,
					mensaje));
		} else {
			logger.log(Logger.FINE, "El agente " + this.getLocalName()
					+ " no dispone de ninguna subasta a la que suscribirse");
		}
	}
}
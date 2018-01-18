package uoc.sma.agentes;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import uoc.sma.behaviours.SubastadorSuscribirBehaviourResponder;
import uoc.sma.behaviours.estrategias.SubastadorHolandesaBehaviour;
import uoc.sma.behaviours.estrategias.SubastadorInglesaBehaviour;
import uoc.sma.datos.Subasta;
import uoc.sma.estrategias.ICfgSubasta;
import jade.core.AID;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionResponder;
import jade.proto.SubscriptionResponder.Subscription;
import jade.proto.SubscriptionResponder.SubscriptionManager;
import jade.util.Logger;

/**
 * Agente que se encarga de una subasta determinada puesta en marcha por un
 * usuario Tiene las responsabilidades siguientes: �]Aceptar las pujas de los
 * compradores y efectuar las postcondiones a la recepcion de una puja
 * �]Notificar a los agentes compradores participantes los cambios de precio
 * en la subasta �]Notificar el ganador
 *
 * @author Rodolfo de Benito Arango
 *
 */
@SuppressWarnings("serial")
public class AgenteSubastador extends AgenteBase {
	private final Logger logger = Logger.getMyLogger(this.getClass().getName());
	private Subasta subasta;
	private ICfgSubasta estrategiaSubasta;
	private Set<Subscription> suscripciones = new HashSet<Subscription>();

	public Subasta getSubasta() {
		return subasta;
	}

	public void setSubasta(Subasta subasta) {
		this.subasta = subasta;
	}

	public Set<Subscription> getSuscripciones() {
		return suscripciones;
	}

	public void setSuscripciones(Set<Subscription> suscripciones) {
		this.suscripciones = suscripciones;
	}

	public ICfgSubasta getEstrategiaSubasta() {
		return estrategiaSubasta;
	}

	/**
	 * Anade el comportamiento con la estrategia que utilizara el comprador
	 *
	 * @param estrategiaSubasta
	 *            La estrategia a seguir en la compra
	 */
	public void setEstrategiaSubasta(ICfgSubasta estrategiaSubasta) {
		this.estrategiaSubasta = estrategiaSubasta;
	}

	@Override
	protected void setup() {
		super.setup();
		// Comprueba los argumentos del agente subastador y obtiene la
		// estrategia del vendedor
		subasta = (Subasta) this.getArguments()[0];
		if ((getArguments() != null) && (getArguments().length >= 2)) {
			setEstrategiaSubasta((ICfgSubasta) getArguments()[1]);
		} else {
			logger.log(Logger.INFO, "El agente " + this.getLocalName()
					+ " no dispone de ninguna estrategia de venta");
			// Termina la ejecucion del agente cuando el numero de argumentos es
			// incorrecto
			this.doDelete();
		}
		logger.log(Logger.FINE, "Agente subastador " + this.getLocalName()
				+ " creado.");
		logger.log(Logger.FINE, this.getLocalName()
				+ ": Esperando suscripciones a la subasta...");
		// Se crea una plantilla para que solo se admitan mensajes del protocolo
		// FIPA�]Subscribe
		MessageTemplate template = SubscriptionResponder
				.createMessageTemplate(ACLMessage.SUBSCRIBE);
		// Se crea un SubscriptionManager que registrara y eliminara las
		// suscripciones.
		SubscriptionManager gestor = crearGestorSubscripciones();
		// Se anade un comportamiento que maneja los mensajes recibidos para
		// suscribirse.
		this.addBehaviour(new SubastadorSuscribirBehaviourResponder(this,
				template, gestor));
		this.addBehaviour(new WakerBehaviour(this, subasta.getFechaInicio()) {
			@Override
			protected void onWake() {
				super.onWake();
				logger.log(Logger.INFO, myAgent.getName()
						+ " �] Se inicia la subasta " + subasta);
				ACLMessage cfpMsg = new ACLMessage(ACLMessage.CFP);
				cfpMsg.setContent(String.valueOf(subasta.getPrecio()));
				cfpMsg.setReplyByDate(new Date(
						System.currentTimeMillis() + 10000));
				if (subasta.getTipoSubasta() == Subasta.SUBASTA_INGLESA)
					myAgent.addBehaviour(new SubastadorInglesaBehaviour(
							myAgent, cfpMsg));
				else if (subasta.getTipoSubasta() == Subasta.SUBASTA_HOLANDESA)
					myAgent.addBehaviour(new SubastadorHolandesaBehaviour(
							myAgent, cfpMsg));
			}
		});
	}

	/**
	 * Crea el gestor de suscripciones y define los metodos para suscribir y
	 * desuscribir agentes a las subastas
	 *
	 * @return
	 */
	private SubscriptionManager crearGestorSubscripciones() {
		SubscriptionManager gestor = new SubscriptionManager() {

			public boolean deregister(Subscription suscripcion)
					throws FailureException {
				if (suscripcion.getMessage().getContent() != null) {
					int idSubasta = obtenerIdSubasta(suscripcion.getMessage()
							.getContent());
					suscripciones.remove(suscripcion);
					return removeSuscriptor(suscripcion.getMessage()
							.getSender(), idSubasta);
				}
				return false;
			}

			public boolean register(Subscription suscripcion)
					throws RefuseException, NotUnderstoodException {
				if (suscripcion.getMessage().getContent() != null) {
					int idSubasta = obtenerIdSubasta(suscripcion.getMessage()
							.getContent());
					suscripciones.add(suscripcion);
					return addSuscriptor(suscripcion.getMessage().getSender(),
							idSubasta);
				}
				return false;
			}

			private int obtenerIdSubasta(String content) {
				return Integer.parseInt(content);
			}
		};
		return gestor;
	}

	@Override
	protected void takeDown() {
		super.takeDown();
	}

	/**
	 * Anade un suscriptor a la lista de suscriptores
	 *
	 * @param suscriptor
	 */
	public boolean addSuscriptor(AID suscriptor, int idSubasta) {
		subasta.getSuscriptores().add(suscriptor);
		logger.log(Logger.FINE,
				"Se ha anadido el suscriptor " + suscriptor.getLocalName()
						+ " a la subasta " + subasta);
		return true;
	}

	/**
	 * Elimina un suscriptor de la lista de suscriptores
	 *
	 * @param suscriptor
	 */
	public boolean removeSuscriptor(AID suscriptor, int idSubasta) {
		subasta.getSuscriptores().remove(suscriptor);
		logger.log(Logger.FINE,
				"Se ha eliminado el suscriptor " + suscriptor.getLocalName()
						+ " de la subasta " + subasta);
		return true;
	}

	// Comprueba la propuesta. En este caso si el contenido del mensaje tiene
	// una longitud mayor que 2, devuelve true
	public boolean compruebaMensaje(String propuesta) {
		return true;
	}

	/**
	 * Anade al mensaje CFP a los suscriptores de la subasta
	 *
	 * @param m
	 *            El mensaje que se enviara
	 * @return
	 */
	public ACLMessage addAgentsToMessage(ACLMessage m) {
		Iterator<AID> it = subasta.getSuscriptores().iterator();
		while (it.hasNext()) {
			m.addReceiver(it.next());
		}
		return m;
	}
}
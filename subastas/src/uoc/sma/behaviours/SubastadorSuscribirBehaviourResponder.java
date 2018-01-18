package uoc.sma.behaviours;

import uoc.sma.agentes.AgenteSubastador;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionResponder;
import jade.util.Logger;

/**
 * Procesa los mensajes relacionados con la suscripcion de Agentes Compradores a
 * una subasta
 *
 * Implementa el protocolo FIPA�]Subscribe con el rol de Participant
 *
 * @author Rodolfo de Benito
 *
 */
@SuppressWarnings("serial")
public class SubastadorSuscribirBehaviourResponder extends
		SubscriptionResponder {
	private final Logger logger = Logger.getMyLogger(this.getClass().getName());

	public SubastadorSuscribirBehaviourResponder(Agent a, MessageTemplate mt,
			SubscriptionManager sm) {
		super(a, mt, sm);
		logger.setLevel(Logger.INFO);
	}

	private Subscription suscripcion;

	/**
	 * Metodo que gestiona la suscripcion aceptando/rechazando las propuestas de
	 * suscripcion a la subasta
	 */
	protected ACLMessage handleSubscription(ACLMessage propuesta)
			throws NotUnderstoodException {
		logger.log(Logger.FINE, myAgent.getLocalName()
				+ ": SUSCRIBE recibido de "
				+ propuesta.getSender().getLocalName() + "\n");
		logger.log(Logger.FINE,
				myAgent.getLocalName()
						+ " La propuesta es suscribirse a la subastas Id:"
						+ myAgent.getLocalName(), propuesta.getContent() + "\n");
		// Comprueba los datos de la propuesta
		if (((AgenteSubastador) myAgent).compruebaMensaje(propuesta
				.getContent())) {
			// Crea la suscripcion
			this.suscripcion = this.createSubscription(propuesta);

			try {
				// El SubscriptionManager registra la suscripcion
				this.mySubscriptionManager.register(suscripcion);
			} catch (Exception e) {
				logger.log(Logger.FINE, myAgent.getLocalName()
						+ ": Error en el registro de la suscripcion.");
			}
			// Acepta la propuesta y la envia
			ACLMessage agree = propuesta.createReply();
			agree.setPerformative(ACLMessage.AGREE);
			return agree;
		} else {
			// Rechaza la propuesta y la envia
			logger.log(Logger.FINE, myAgent.getLocalName()
					+ ": Rechaza la propuesta de suscripcion.");
			ACLMessage refuse = propuesta.createReply();
			refuse.setPerformative(ACLMessage.REFUSE);
			return refuse;
		}
	}

	/**
	 * Gestiona la cancelacion de la subasta
	 */
	protected ACLMessage handleCancel(ACLMessage cancelacion) {
		logger.log(Logger.FINE, myAgent.getLocalName()
				+ "%s: CANCEL recibido de "
				+ cancelacion.getSender().getLocalName() + "\n");
		try {
			// El SubscriptionManager elimina del registro la suscripcion
			this.mySubscriptionManager.deregister(this.suscripcion);
		} catch (Exception e) {
			logger.log(
					Logger.FINE,
					myAgent.getLocalName()
							+ ": Error en la operacion de anluar la suscripcion de la suscripcion.");
		}
		// Acepta la cancelacion y responde
		ACLMessage cancela = cancelacion.createReply();
		cancela.setPerformative(ACLMessage.INFORM);
		return cancela;
	}
}
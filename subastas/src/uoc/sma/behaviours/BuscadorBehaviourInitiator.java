package uoc.sma.behaviours;

import java.io.IOException;
import java.util.Vector;
import uoc.sma.datos.Subasta;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.util.Logger;

/**
 * Comportamiento del Agente buscador. Forma parte del protocolo
 * FIPA�]Brokering
 *
 * Recibe: �]Mensajes del Agente CasaSubastas que contienen el resultado de la
 * busqueda
 *
 * Envia: �]Mensajes de peticion al Agente CasaSubastas solicitando una
 * busqueda de subastas
 *
 * �]Mensajes de respuesta al Agente Comprador con el resultado de la busqueda
 *
 * @author Rodofo de Benito
 *
 */
public class BuscadorBehaviourInitiator extends AchieveREInitiator {
	private final Logger logger = Logger.getMyLogger(this.getClass().getName());
	private AID idCasaSubastas;

	public BuscadorBehaviourInitiator(Agent a, ACLMessage mt) {
		super(a, mt);
		logger.setLevel(Logger.INFO);
		// Localizar al Agente de Subastas en las paginas amarillas
		DFAgentDescription[] aCasaSubastas = getCasaSubastas();
		if (aCasaSubastas != null)
			this.idCasaSubastas = aCasaSubastas[0].getName();
	}

	/**
	 * Construye la pregunta al Agente CasaSubastas en base a la pregunta del
	 * Agente Comprador
	 */
	protected Vector prepareRequests(ACLMessage request) {
		// Recupera la peticion entrante del DataStore
		String clavePeticionEntrada = (String) ((AchieveREResponder) parent).REQUEST_KEY;
		ACLMessage peticionEntrada = (ACLMessage) getDataStore().get(
				clavePeticionEntrada);

		// Prepara la peticion para enviarsela al Agente CasaSubastas
		logger.log(Logger.FINE, "Agente " + myAgent.getLocalName()
				+ ": Redirigiendo the peticion a " + idCasaSubastas.getName());
		ACLMessage peticionSalida = new ACLMessage(ACLMessage.QUERY_REF);
		peticionSalida.setProtocol(FIPANames.InteractionProtocol.FIPA_QUERY);
		peticionSalida.addReceiver(idCasaSubastas);
		peticionSalida.setContent(peticionEntrada.getContent());
		peticionSalida.setReplyByDate(peticionEntrada.getReplyByDate());
		Vector v = new Vector(1);
		v.addElement(peticionSalida);
		return v;
	}

	protected void handleInform(ACLMessage inform) {
		guardarMensaje(ACLMessage.INFORM);
	}

	protected void handleRefuse(ACLMessage refuse) {
		guardarMensaje(ACLMessage.FAILURE);
	}

	protected void handleNotUnderstood(ACLMessage notUnderstood) {
		guardarMensaje(ACLMessage.FAILURE);
	}

	protected void handleFailure(ACLMessage failure) {
		guardarMensaje(ACLMessage.FAILURE);
	}

	protected void handleAllResultNotifications(Vector notificaciones) {
		if (notificaciones.size() == 0) {
			// Timeout
			guardarMensaje(ACLMessage.FAILURE);
		}
	}

	/**
	 * Trata los mensajes recibidos y gestiona el Datastore para recuperar los
	 * mensajes recibidos y almacenar mensajes de respuesta
	 *
	 * @param performative
	 */
	private void guardarMensaje(int performative) {
		Subasta subasta = null;
		if (performative == ACLMessage.INFORM) {
			logger.log(Logger.INFO, "Agente " + myAgent.getLocalName()
					+ ": Redireccion conseguida");
		} else {
			logger.log(Logger.INFO, "Agente " + myAgent.getLocalName()
					+ ": Fallo en la redireccion ");
		}
		// Recupera la peticion entrante del DataStore. El mensaje del agente
		// comprador
		String clavePeticionEntrada = (String) ((AchieveREResponder) parent).REQUEST_KEY;
		ACLMessage peticionEntrada = (ACLMessage) getDataStore().get(
				clavePeticionEntrada);
		// Recupera del DataStore la respuesta del agente CasaDeSubastas
		ACLMessage respuestaCasaSubastas = (ACLMessage) getDataStore().get(
				REPLY_KEY);
		// Prepara el mensaje para el Agente Comprador y la almacena en el
		// DataStore
		ACLMessage mensaje = peticionEntrada.createReply();
		mensaje.setPerformative(performative);
		try {
			if (respuestaCasaSubastas.getContentObject() != null) {

				subasta = (Subasta) respuestaCasaSubastas.getContentObject();
				mensaje.setContentObject(subasta);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnreadableException e) {
			e.printStackTrace();
		}
		String notificationkey = (String) ((AchieveREResponder) parent).RESULT_NOTIFICATION_KEY;
		getDataStore().put(notificationkey, mensaje);
	}

	/**
	 * Localiza al agente CasaSubastas en las paginas amarillas
	 * 
	 * @return
	 */
	private DFAgentDescription[] getCasaSubastas() {
		DFAgentDescription template = new DFAgentDescription();
		DFAgentDescription[] result = null;
		ServiceDescription sd = new ServiceDescription();
		sd.setType("CasaDeSubastas");
		template.addServices(sd);
		try {
			result = DFService.search(myAgent, template);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		return result;
	}
}
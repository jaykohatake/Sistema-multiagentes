package uoc.sma.behaviours.estrategias;

import java.io.IOException;
import uoc.sma.behaviours.SubastadorTransaccionesBehaviourInitiator;
import uoc.sma.datos.Transaccion;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

@SuppressWarnings("serial")
public class SubastadorBehaviour extends ContractNetInitiator {
	public SubastadorBehaviour(Agent a, ACLMessage cfp) {
		super(a, cfp);
	}

	/**
	 * Env�a un mensaje al agente CasaSubastas para que registre la
	 * transacci�n
	 *
	 * @param t
	 *            La transacci�n
	 * @param idCasaSubastas
	 *            El identificador del agente CasaSubastas
	 */
	protected void registrarTransaccion(Transaccion t, AID idCasaSubastas) {
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		msg.addReceiver(idCasaSubastas);
		try {
			msg.setContentObject(t);
		} catch (IOException e) {
			e.printStackTrace();
		}
		myAgent.addBehaviour(new SubastadorTransaccionesBehaviourInitiator(
				myAgent, msg));
	}

	/**
	 * Obtiene el identificador del agente CasaSubastas de las p�ginas
	 * amarillas
	 * 
	 * @return
	 */
	protected DFAgentDescription[] getCasaSubastas() {
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
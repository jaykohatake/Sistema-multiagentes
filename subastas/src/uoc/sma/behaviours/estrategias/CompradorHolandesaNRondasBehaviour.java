package uoc.sma.behaviours.estrategias;

import uoc.sma.agentes.AgenteComprador;
import uoc.sma.estrategias.ApuestaUnicaNRondasCfgImpl;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.proto.SSIteratedContractNetResponder;
import jade.util.Logger;

/**
 * Estrategia del comprador de la subasta holandesa en la que al principio el
 * agente considera pujar en la subasta por el precio configurado en su atributo
 * Puja. Si en el transcurso de la subasta se supera el n�mero de rondas
 * configurado en la estrategia entonces el valor de aceptaci�n de la puja
 * ser� el de su precio de reserva.
 *
 * @author Rodolfo de Benito
 *
 */
@SuppressWarnings("serial")
public class CompradorHolandesaNRondasBehaviour extends
		SSIteratedContractNetResponder {
	private final Logger logger = Logger.getMyLogger(this.getClass().getName());
	private AgenteComprador agente;
	private ApuestaUnicaNRondasCfgImpl estrategia;
	private double puja = 0;

	public CompradorHolandesaNRondasBehaviour(Agent a, ACLMessage cfp) {
		super(a, cfp);
		logger.setLevel(Logger.INFO);
		agente = (AgenteComprador) a;
		estrategia = (ApuestaUnicaNRondasCfgImpl) agente.getEstrategia();
	}

	@Override
	/**
	 * Trata las propuestas de compra del subastastador y env�a un mensaje
	 * PROPOSE en caso que acepte la oferta de venta o un mensaje REFUSE si
	 * rechaza la oferta
	 */
	protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException,
			FailureException, NotUnderstoodException {
		ACLMessage firstPropose = cfp.createReply();
		estrategia.setContadorRondas(estrategia.getContadorRondas() + 1);
		puja = Double.parseDouble(cfp.getContent());
		if (pujaAceptable(puja)) {
			// Se acepta la puja
			firstPropose.setPerformative(ACLMessage.PROPOSE);
			logger.log(Logger.FINE, myAgent.getLocalName()
					+ " : ENVIA : PROPOSE : " + puja);
			firstPropose.setContent(String.valueOf(puja));

		} else {
			// No puja
			firstPropose.setPerformative(ACLMessage.REFUSE);
			logger.log(Logger.FINE, myAgent.getLocalName()
					+ " : ENVIA : REFUSE : " + cfp.getContent());
		}
		return firstPropose;
	}

	@Override
	/**
	 * Trata los mensajes de rechazo sobre una propuesta enviada al comprador
	 */
	protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose,
			ACLMessage reject) {
		logger.log(Logger.FINE, myAgent.getLocalName() + " : Recibe : REJECT");
		if (reject == null) {
			logger.log(Logger.FINE, myAgent.getLocalName()
					+ " : Responder : TIMEOUT");
		}
	}

	@Override
	/**
	 * Trata los mensajes de aceptaci�n de una propuesta de compra por parte del
	 * comprador.
	 *
	 * Env�a un mensaje INFORM cuando el subastador acepta la propuesta de
	 * compra.
	 *
	 */
	protected ACLMessage handleAcceptProposal(ACLMessage cfp,
			ACLMessage propose, ACLMessage accept) throws FailureException {
		logger.log(Logger.FINE, myAgent.getName() + " : Recibe : ACCEPT");
		if (accept != null) {
			ACLMessage inform = accept.createReply();
			inform.setPerformative(ACLMessage.INFORM);
			inform.setContent(String.valueOf(estrategia.getPuja()));
			logger.log(Logger.FINE, myAgent.getLocalName()
					+ " : Env�a : INFORM : " + puja);
			return inform;
		} else {
			ACLMessage refuse = cfp.createReply();
			refuse.setPerformative(ACLMessage.REFUSE);
			refuse.setContent(String.valueOf(estrategia.getPuja()));
			logger.log(Logger.FINE, myAgent.getLocalName()
					+ " : Env�a : REFUSE");
			return refuse;
		}
	}

	/**
	 * Comprueba si la puja propuesta por el agente comprador es aceptable y si
	 * el n�mero de rondas es mayor o igual que el establecido entonces
	 * incrementa el valor de la puja aceptable
	 *
	 * @param puja
	 *            El precio propuesto por el agente vendedor
	 * @return
	 */
	private boolean pujaAceptable(double puja) {
		if (puja <= estrategia.getPuja())

			return true;
		if (estrategia.getContadorRondas() >= estrategia.getRondas()
				&& estrategia.getReserva() != estrategia.getPuja()) {
			// Transcurridas n rodas se iguala la puja al precio de reserva
			estrategia.setPuja(estrategia.getReserva());
			logger.log(
					Logger.FINE,
					myAgent.getLocalName()
							+ ": se iguala el valor de la nueva puja aceptable al de la reserva:"
							+ estrategia.getReserva());
			return false;
		} else
			return false; // No interesa pujar
	}
}
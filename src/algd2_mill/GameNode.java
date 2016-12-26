package algd2_mill;
import java.util.Random;

public abstract class GameNode extends Node<IAction> implements IGameNode {

	private int m_score;
	
	public GameNode(IAction data) {
		super(data);
	}
	
	public GameNode(IAction data, int score) {
		super(data);
		m_score = score;
	}
	
	@Override
	public GameNode add(IAction a, int score) {
		GameNode n;
		// A MinNode is a child of a MaxNode and vice versa
		if (this instanceof MinNode) {
			n = new MaxNode(a, score);
		}
		else {
			n = new MinNode(a, score);
		}
		add(n);
		return n;
	}

	@Override
	public GameNode add(IAction a) {
		return add(a, 0);
	}

	@Override
	public int create(int curHeight, int height, byte color, GameNode root, State rootState) {
		assert curHeight <= height;
		if (curHeight == height) return 0; // no more nodes need to be created
		
		// else add all possible immediate follow-up actions (meaning 1 level below in the tree)
		int nodesAdded = 0;
		if (rootState.placingPhase(color)) {
			for (byte pos = 0; pos < State.NPOS; pos++) { // try all possible Placing actions
				if (rootState.isValidPlace(pos, color)) { 
					ActionPM a = new Placing(color, pos);
					if (!rootState.inMill(pos, color)) { // if Placing does not result in a mill
						root.add(a);
						nodesAdded++;
					}
					else if(rootState.takingIsPossible(color)) { // if Placing does result in a mill: it can become several Taking actions
						for (byte takepos = 0; takepos < State.NPOS; takepos++) {
							if (rootState.isValidTake(takepos, color)) {
								root.add(new Taking(a, takepos));
								nodesAdded++;
							}
						}
					}
				}
			}
		}
		else if (rootState.movingPhase(color)) {

		}
		else if (rootState.jumpingPhase(color)) {
	
		}
		
		if (++curHeight == height) return nodesAdded;
		
		// if tree is not deep enough after adding 1 more level:
		for (Object n : root.m_children.toArray()) { // add 1 more level on each child (toArray() to avoid ConcurrentModificationException)
			nodesAdded += create(curHeight, height, State.oppositeColor(color), (GameNode)n, ((GameNode)n).computeState(rootState.clone(), root));
			// clone() so original state does not get changed
		}
		return nodesAdded;

	}

	@Override
	public State computeState(State s, GameNode v) {
		if (m_parent == v) {
			m_data.update(s);
			return s;
		}
		State parentState = ((GameNode)m_parent).computeState(s, v);
		m_data.update(parentState);
		return parentState;	
	}

	@Override
	public int score() {
		// TODO
		return (int)(Math.random()*100000);
	}

}
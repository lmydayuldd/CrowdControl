package uk.ac.cam.bravo.CrowdControl.agent;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.AgentManagerInterface;
import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.DoorInterface;
import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.RoomInterface;

public class ParticleAgent implements AgentInterface {
	// The agent manager.
	private AgentManagerInterface agentManager;
	// Sequence of goal rooms and wait times, not including the very next one.
	private List<ItineraryItemInterface> itinerary;
	// Current and desired room.
	private RoomWrapper room;
	private RoomWrapper desiredRoom;
	// Time to wait when in desired room.
	private int waitTime;
	// Current and desired position within room.
	private Vector2D position;
	private Vector2D desiredPosition;
	// Door to pass through.
	private DoorInterface nextDoor;
	// The agent's normal and emergency walking speed.
	public static float normalSpeed = 25f, emergencySpeed = normalSpeed * 2f,
	                     injuredSpeed = 3f * normalSpeed / 5f;
	// The force with which an agent moves.
	public static float walkForce = normalSpeed * 0.5f;
	// The force between agents.
	public static float interactionForce = walkForce * 50f;
	// The 'drag' factor of an agent's movement.
	public static float dragFactor = 0.5f;
	// Pressures to cause injury and death.
	public static float injuryPressure = 2000f, deathPressure = 3000f;
	// Distance at which agents are reacted to.
	public static float interactionDistance = 100f;
	// The agent's current walking speed.
	private float desiredSpeed;
	// Current net 'force' and 'pressure' on agent.
	private Vector2D force;
	private float pressure;
	// Agent's health state.
	private Health health;
	// Change in position in last tick.
	private Vector2D velocity;
	// Cumulative distance and time since last call to ResetAverageSpeed.
	private float moveDistance;
	private int moveTime;
	private int itineraryTime;
	// An indication of how long we have been 'slow' in getting to the door. 
	private float slowTime;
	// The door that slowTime refers to.
	private DoorInterface slowDoor;

	private boolean evacuating = false;
	
	public ParticleAgent(AgentManagerInterface agentManager, RoomInterface room, Point2D.Float position, List<ItineraryItemInterface> itinerary) {
		this.agentManager = agentManager;
		this.itinerary = new ArrayList<ItineraryItemInterface>(itinerary);
		this.desiredRoom = this.room = new RoomWrapper(room);
		this.waitTime = 0;
		this.position = new Vector2D(position);
		this.desiredPosition = new Vector2D(position);
		this.nextDoor = null;
		this.desiredSpeed = normalSpeed;
		this.force = new Vector2D(0, 0);
		this.pressure = 0;
		this.health = Health.Healthy;
		this.velocity = new Vector2D(0, 0);
		this.moveDistance = 0;
		this.moveTime = 0;
		this.itineraryTime = 0;
		this.slowTime = 0;
		this.slowDoor = null;
	}

	public Point2D.Float GetPosition() {
		return new Vector2D(position);
	}

	public RoomInterface GetRoom() {
		return room.getRoom();
	}

	public List<ItineraryItemInterface> GetItinerary() {
		boolean haveItinerary = (itinerary != null && !itinerary.isEmpty());
		boolean addCurrent = (!desiredRoom.isNull() && !desiredRoom.equals(room)) || waitTime > 0;
		int size = addCurrent ? 1 : 0;
		if(haveItinerary)
			size += itinerary.size();
		ArrayList<ItineraryItemInterface> result = new ArrayList<ItineraryItemInterface>(size);
		if(addCurrent)
			result.add(new ItineraryItem((desiredRoom.isNull() ? room : desiredRoom).getRoom(), waitTime));
		if(haveItinerary)
			result.addAll(itinerary);
		return result;
	}

	public void SetItinerary(List<ItineraryItemInterface> newItinerary) {
		itinerary = new ArrayList<ItineraryItemInterface>(newItinerary);
		itineraryTime = 0;
		desiredRoom = room;
		waitTime = 0;
		desiredPosition = null;
		nextDoor = null;
	}

	public void Act() {
		UpdateGoals();
		UpdateForces();
		UpdateHealth();
		DoMove();
		UpdateStatistics();
	}

	public void SetEmergency() {
		itinerary = null;
		itineraryTime = 0;
		desiredRoom = new RoomWrapper(agentManager.getOutside(this));
		waitTime = 0;
		desiredPosition = position;
		nextDoor = null;
		evacuating = true;
		if (health == Health.Healthy)	// don't change speed if we are already injured or dead
			desiredSpeed = emergencySpeed;
	}

	public Point2D.Float GetLastMovement() {
		return new Vector2D(velocity);
	}

	public float GetAverageSpeed() {
		if(moveTime == 0)
			return 0;
		return moveDistance / moveTime;
	}

	public void ResetAverageSpeed() {
		moveDistance = moveTime = 0;
		// Do not reset itineraryTime here.
	}
	
	public int timeTakenForItinerary() {
		return itineraryTime;
	}
	
	public void UpdateStatistics() {
		if(nextDoor != slowDoor) {
			slowTime = 0;
			slowDoor = nextDoor;
		}
		boolean targettingRoom = IsPurposeful();
		boolean hasItinerary = (itinerary != null && !itinerary.isEmpty());
		if(targettingRoom) {
			++moveTime;
			moveDistance += velocity.Length();
		}
		if(targettingRoom || hasItinerary)
			++itineraryTime;
		if(targettingRoom && velocity.Length() <= GetSlowSpeed())
			slowTime += 1;
		else
			slowTime = Math.max(0, slowTime - 0.0625f);
	}

	public Health GetHealth() {
		return health;
	}
	
	protected boolean IsPurposeful() {
		return health != Health.Dead && !desiredRoom.isNull() && !room.equals(desiredRoom);
	}

	protected void SetRoom(RoomInterface room) {
		RoomInterface oldRoom = this.room.getRoom();
		if(room != oldRoom) {
			this.room = new RoomWrapper(room);
			agentManager.updateCurrentRoom(this, oldRoom, room);
		}
	}

	protected void SetDesiredPosition(Vector2D pos) {
		desiredPosition = pos;
	}
	
	protected float GetSlowSpeed() {
		return Math.min(desiredSpeed, walkForce / dragFactor) * 0.5f;
	}

	protected void UpdateHealth() {
		if(health != Health.Dead) {
			if(pressure >= deathPressure)
				health = Health.Dead;
			else if(pressure >= injuryPressure)
				health = Health.Injured;
				desiredSpeed = injuredSpeed;
		}
	}

	protected float GetGoalDistance(int x, int y) {
		if(desiredPosition == null)
			return 0;
		return room.getGoalDistance(x, y, room.posToGridX(desiredPosition.x), room.posToGridY(desiredPosition.y));
	}

	protected void DoMove() {
		// Save old velocity and determine new velocity.
		Vector2D oldVelocity = velocity;
		velocity = oldVelocity.Scale(1 - dragFactor).Add(force);
		float expectedSpeed = velocity.Length();
		// Stop idle movement if we are slow or oscillating.
		if(!IsPurposeful() && (expectedSpeed <= GetSlowSpeed() || velocity.x * oldVelocity.x + velocity.y * oldVelocity.y < 0))
			SetDesiredPosition(null);
		// Store old position and compute new position.
		Vector2D oldPosition = position;
		position = oldPosition.Add(velocity);
		// Handle waiting and changing rooms.
		if(room.equals(desiredRoom) && waitTime > 0) {
			--waitTime;
		} else if(nextDoor != null) {
			Line2D.Float doorLine = nextDoor.getRoomCoord(room.getRoom());
			boolean throughDoor = doorLine.intersectsLine(oldPosition.x, oldPosition.y, position.x, position.y); 
			if(throughDoor || doorLine.ptLineDist(position) < slowTime) {
				RoomInterface newRoom = nextDoor.getDestination(room.getRoom());
				// TODO: Ideally we would properly transform the position into the new room's coordinates.
				// For now we keep the same position if the coordinates are the same, and move to the middle of the door otherwise.
				oldPosition = GetLineMidpoint(nextDoor.getRoomCoord(newRoom));
				if(!throughDoor || !nextDoor.getRoomCoord(newRoom).equals(doorLine))
					position = oldPosition;
				velocity = position.Subtract(oldPosition);
				// Make sure that going through the door has not caused us to speed up.
				if(velocity.Length() > expectedSpeed) {
					velocity = velocity.Normalise().Scale(expectedSpeed);
					position = oldPosition.Add(velocity);
				}
				SetRoom(newRoom);
				nextDoor = null;
				SetDesiredPosition(null);
			}
		}
		// Try not to get stuck when doors show up as unreachable.
		if(IsPurposeful() && GetGoalDistance(room.posToGridX(oldPosition.x), room.posToGridY(oldPosition.y)) == Float.POSITIVE_INFINITY) {
			float speedLimit = GetSlowSpeed();
			if(velocity.Length() > speedLimit) {
				// Still try to avoid zooming off into walls.
				velocity = velocity.Normalise().Scale(speedLimit);
				position = oldPosition.Add(velocity);
			}
			return;
		}
		// Avoid walking into obstacles unless we are already in one.
		final int startX = room.posToGridX(oldPosition.x);
		final int startY = room.posToGridY(oldPosition.y);
		final int endX = room.posToGridX(position.x);
		final int endY = room.posToGridX(position.y);
		final int steps = Math.max(1, Math.max(endX - startX, endY - startY));
		final Vector2D origVelocity = velocity;
		for(int step = 0; step <= steps; ++step) {
			final Vector2D tryVelocity = origVelocity.Scale((float) step / steps);
			final Vector2D tryPosition = oldPosition.Add(tryVelocity);
			if(!room.isSquarePassable(room.posToGridX(tryPosition.x), room.posToGridY(tryPosition.y))) {
				// Stop idle movement if we are encountering an obstacle.
				if(!IsPurposeful())
					SetDesiredPosition(null);
				break;
			}
			velocity = tryVelocity;
			position = tryPosition;
		}
	}

	protected void UpdateGoals() {
		Vector2D newDesiredPosition = desiredPosition;
		if(room.equals(desiredRoom)) {
			nextDoor = null;
			if (evacuating) {
				desiredRoom = new RoomWrapper(agentManager.getOutside());
			} else if(waitTime <= 0 && itinerary != null && !itinerary.isEmpty()) {
				ItineraryItemInterface item = itinerary.remove(0);
				desiredRoom = new RoomWrapper(item.GetRoom());
				waitTime = item.GetWaitTime();
			}
		}
		if(nextDoor == null && !room.equals(desiredRoom)) {
			List<DoorInterface> suggestedDoors = agentManager.getHighLevelPath(room.getRoom(), desiredRoom.getRoom(), GetPosition());
			if(suggestedDoors != null && !suggestedDoors.isEmpty())
				nextDoor = suggestedDoors.get(0);
		}
		if(nextDoor != null) {
			newDesiredPosition = GetLineMidpoint(nextDoor.getRoomCoord(room.getRoom()));
		} else if(newDesiredPosition == null) {
			Rectangle2D.Float boundingBox = room.getBoundingBox();
			if(boundingBox != null) {
				newDesiredPosition = new Vector2D(
						(float) (boundingBox.x + Math.random() * boundingBox.width),
						(float) (boundingBox.y + Math.random() * boundingBox.height)
				);
			}
		}
		SetDesiredPosition(newDesiredPosition);
	}

	protected void UpdateForces() {
		ArrayList<Vector2D> forceList = new ArrayList<Vector2D>();
		if (health != Health.Dead) // Only living agents can move themselves.
			GetGoalForce(forceList);
		if (room.isPointPassable(position)) // Avoid agents getting pushed out into walls.
			GetAgentForce(forceList);
		force = new Vector2D();
		pressure = 0;
		for(Vector2D partForce : forceList) {
			if(Float.isInfinite(partForce.x) || Float.isNaN(partForce.x) || Float.isInfinite(partForce.y) || Float.isNaN(partForce.y))
				continue;
			force = force.Add(partForce);
			pressure += partForce.Length();
		}
	}

	protected boolean AddGoalMoveDirection(Vector2D result, int moveX, int moveY, int baseX, int baseY) {
		float pref = 1 / GetGoalDistance(baseX + moveX, baseY + moveY);
		if(pref == Float.POSITIVE_INFINITY)
			return true;
		if(!Float.isInfinite(pref) && !Float.isNaN(pref)) {
			result.x += moveX * pref;
			result.y += moveY * pref;
		}
		return false;
	}
	protected void AddObstacleMoveDirection(Vector2D result, int moveX, int moveY, int baseX, int baseY) {
		float pref = -1 / room.getObstacleDistance(baseX + moveX, baseY + moveY);
		if(pref == Float.NEGATIVE_INFINITY)
			pref = 1;
		if(!Float.isInfinite(pref) && !Float.isNaN(pref)) {
			result.x += moveX * pref;
			result.y += moveY * pref;
		}
	}
	protected void GetGoalForce(List<Vector2D> forceList) {
		Vector2D goalV = new Vector2D();
		if(desiredPosition != null) {
			int squareX = room.posToGridX(position.x);
			int squareY = room.posToGridY(position.y);
			boolean goalInVicinity = 
				!IsPurposeful() || // Avoid expensive computation for idle movements.
				AddGoalMoveDirection(goalV, -1, -1, squareX, squareY) ||
				AddGoalMoveDirection(goalV, -1, 0, squareX, squareY) ||
				AddGoalMoveDirection(goalV, -1, 1, squareX, squareY) ||
				AddGoalMoveDirection(goalV, 0, -1, squareX, squareY) ||
				AddGoalMoveDirection(goalV, 0, 0, squareX, squareY) ||
				AddGoalMoveDirection(goalV, 0, 1, squareX, squareY) ||
				AddGoalMoveDirection(goalV, 1, -1, squareX, squareY) ||
				AddGoalMoveDirection(goalV, 1, 0, squareX, squareY) ||
				AddGoalMoveDirection(goalV, 1, 1, squareX, squareY);
			if(goalInVicinity || goalV.IsZero()) {
				goalV = desiredPosition.Subtract(position);
			} else {
				Vector2D obstacleV = new Vector2D();
				AddObstacleMoveDirection(obstacleV, -1, -1, squareX, squareY);
				AddObstacleMoveDirection(obstacleV, -1, 0, squareX, squareY);
				AddObstacleMoveDirection(obstacleV, -1, 1, squareX, squareY);
				AddObstacleMoveDirection(obstacleV, 0, -1, squareX, squareY);
				AddObstacleMoveDirection(obstacleV, 0, 0, squareX, squareY);
				AddObstacleMoveDirection(obstacleV, 0, 1, squareX, squareY);
				AddObstacleMoveDirection(obstacleV, 1, -1, squareX, squareY);
				AddObstacleMoveDirection(obstacleV, 1, 0, squareX, squareY);
				AddObstacleMoveDirection(obstacleV, 1, 1, squareX, squareY);
				goalV = goalV.Normalise().Add(obstacleV.Normalise().Scale(0.5f));
			}
		} else {
			goalV = new Vector2D((float) Math.random() - 0.5f, (float) Math.random() - 0.5f);
		}

		goalV = goalV.Normalise().Scale(desiredSpeed);
		Vector2D force = goalV.Subtract(velocity.Scale(1 - dragFactor));
		if(force.Length() > walkForce)
			force = force.Normalise().Scale(walkForce);
		forceList.add(force);
	}
	protected void GetAgentForce(List<Vector2D> forceList) {
		for(AgentInterface agent: agentManager.getAgentsNear(room.getRoom(), position, interactionDistance)) {
			if(agent == null || agent.GetRoom() != room.getRoom() || (health == Health.Dead && agent.GetHealth() == Health.Dead))
				continue;
			Vector2D offset = position.Subtract(agent.GetPosition());
			float len = offset.Length();
			if(!offset.IsZero() && len <= interactionDistance)
				forceList.add(offset.Normalise().Scale((float) (interactionForce / (Math.sqrt(slowTime) + 1) / (len + 1))));
		}
	}

	protected Vector2D GetLineMidpoint(Line2D.Float line) {
		return new Vector2D((line.x1 + line.x2) / 2, (line.y1 + line.y2) / 2);
	}
}


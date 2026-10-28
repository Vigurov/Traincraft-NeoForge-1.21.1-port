package fexcraft.tmt.slim;

/**
* Converter to use Flansmod-Type vehicle models.
* @Author Ferdinand Calo' (FEX___96)
*/
public class ModelConverter extends ModelBase {

	public ModelRendererTurbo model[],bodyDoorOpenModel[], bodyDoorCloseModel[],trailerModel[],steeringWheelModel[];
	
	public ModelRendererTurbo turretModel[],barrelModel[];
	public ModelRendererTurbo frontWheelModel[],backWheelModel[],
			leftFrontWheelModel[],rightFrontWheelModel[],leftBackWheelModel[],rightBackWheelModel[];
	public ModelRendererTurbo rightTrackModel[],leftTrackModel[],
			rightTrackWheelModels[],leftTrackWheelModels[];

	@Override
	public void initAllParts(){
		model=initList(model);
		bodyDoorOpenModel=initList(bodyDoorOpenModel);
		bodyDoorCloseModel=initList(bodyDoorCloseModel);
		trailerModel=initList(trailerModel);
		steeringWheelModel=initList(steeringWheelModel);
		turretModel=initList(turretModel);
		barrelModel=initList(barrelModel);
		frontWheelModel=initList(frontWheelModel);
		backWheelModel=initList(backWheelModel);
		leftFrontWheelModel=initList(leftFrontWheelModel);
		rightFrontWheelModel=initList(rightFrontWheelModel);
		leftBackWheelModel=initList(leftBackWheelModel);
		rightBackWheelModel=initList(rightBackWheelModel);
		rightTrackModel=initList(rightTrackModel);
		leftTrackModel=initList(leftTrackModel);
		rightTrackWheelModels=initList(rightTrackWheelModels);
		leftTrackWheelModels=initList(leftTrackWheelModels);
		super.initAllParts();
	}


	@Override
	public void translateAll(float x, float y, float z){
		super.translateAll(x,y,z);
		translate(model, x, y, z);
		translate(bodyDoorOpenModel, x, y, z);
		translate(bodyDoorCloseModel, x, y, z);
		translate(turretModel, x, y, z);
		translate(barrelModel, x, y, z);
		translate(frontWheelModel, x, y, z);
		translate(backWheelModel, x, y, z);
		translate(leftFrontWheelModel, x, y, z);
		translate(rightFrontWheelModel, x, y, z);
		translate(leftBackWheelModel, x, y, z);
		translate(rightBackWheelModel, x, y, z);
		translate(rightTrackModel, x, y, z);
		translate(leftTrackModel, x, y, z);
		translate(rightTrackWheelModels, x, y, z);
		translate(leftTrackWheelModels, x, y, z);
		translate(trailerModel, x, y, z);
		translate(steeringWheelModel, x, y, z);
	}

	@Override
	public void rotateAll(float x, float y, float z){
		super.rotateAll(x,y,z);
		rotate(model, x, y, z);
		rotate(bodyDoorOpenModel, x, y, z);
		rotate(bodyDoorCloseModel, x, y, z);
		rotate(turretModel, x, y, z);
		rotate(barrelModel, x, y, z);
		rotate(frontWheelModel, x, y, z);
		rotate(backWheelModel, x, y, z);
		rotate(leftFrontWheelModel, x, y, z);
		rotate(rightFrontWheelModel, x, y, z);
		rotate(leftBackWheelModel, x, y, z);
		rotate(rightBackWheelModel, x, y, z);
		rotate(rightTrackModel, x, y, z);
		rotate(leftTrackModel, x, y, z);
		rotate(rightTrackWheelModels, x, y, z);
		rotate(leftTrackWheelModels, x, y, z);
		rotate(trailerModel, x, y, z);
		rotate(steeringWheelModel, x, y, z);
	}

	/**
	 * squeezes those parts of bodyModel across the model toward z = wall (in the model's own units, before flipAll):
	 * what reached from the wall to z = from reaches to z = to (a bench as wide as the car made a seat for one, the
	 * aisle beside it wider). Each vertex once; a part turned about x or y is moved, not squeezed.
	 */
	public void squeezeAcross(int[] parts, float wall, float from, float to){
		float s = (to - wall) / (from - wall);
		java.util.Set<Vec3f> done = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
		for (int k : parts) {
			ModelRendererTurbo part = bodyModel[k];
			if (part == null) continue;
			//turned half round about y (radians): its z the other way; turned otherwise about x or y: moved as a whole
			float sign = 1;
			if (part.rotateAngleX == 0 && Math.abs(Math.abs(part.rotateAngleY) - (float) Math.PI) < 0.01f) sign = -1;
			else if (part.rotateAngleX != 0 || part.rotateAngleY != 0) {
				part.rotationPointZ += wall + (part.rotationPointZ - wall) * s - part.rotationPointZ;
				continue;
			}
			//(a turn about z, a leaning back, leaves z as it is)
			for (TexturedPolygon poly : part.faces) {
				for (TexturedVertex v : poly.vertices) {
					if (!done.add(v.vector3F)) continue;
					float z = part.rotationPointZ + sign * v.vector3F.zCoord;
					v.vector3F.zCoord = sign * (wall + (z - wall) * s - part.rotationPointZ);
				}
			}
		}
	}

	/** from..to, both included */
	public static int[] range(int[]... spans){
		java.util.List<Integer> l = new java.util.ArrayList<>();
		for (int[] r : spans) for (int k = r[0]; k <= r[r.length - 1]; k++) l.add(k);
		int[] a = new int[l.size()];
		for (int k = 0; k < a.length; k++) a[k] = l.get(k);
		return a;
	}

	@Override
	public void flipAll(){
		super.flipAll();
		flip(model);
		flip(bodyDoorOpenModel);
		flip(bodyDoorCloseModel);
		flip(turretModel);
		flip(barrelModel);
		flip(frontWheelModel);
		flip(backWheelModel);
		flip(leftFrontWheelModel);
		flip(rightFrontWheelModel);
		flip(leftBackWheelModel);
		flip(rightBackWheelModel);
		flip(rightTrackModel);
		flip(leftTrackModel);
		flip(rightTrackWheelModels);
		flip(leftTrackWheelModels);
		flip(trailerModel);
		flip(steeringWheelModel);
	}
	
}

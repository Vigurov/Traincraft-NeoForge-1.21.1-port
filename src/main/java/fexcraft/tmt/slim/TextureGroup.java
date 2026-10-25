package fexcraft.tmt.slim;

import java.util.ArrayList;

public class TextureGroup {

	public ArrayList<TexturedPolygon> poly;
	public String texture;

	public TextureGroup(){
		poly = new ArrayList<TexturedPolygon>();
		texture = "";
	}

	public void addPoly(TexturedPolygon quad){
		poly.add(quad);
	}
}

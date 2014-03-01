package com.example.opengl_example.parser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import com.example.opengl_example.R;

import android.content.Context;

public class MTLParser {

	public  static Vector<Material> loadMTL(String file,Context context){
		BufferedReader reader=null;
		Vector<Material> materials=new Vector<Material>();
		String line;
		Material currentMtl=null;
		
		//reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		
		InputStream fileIn = context.getResources().openRawResource(R.raw.camaro_mtl);
		reader = new BufferedReader(
				new InputStreamReader(fileIn));
		
		
		if(reader!=null){
			try {
				while((line = reader.readLine()) != null) {
					if(line.startsWith("newmtl")){
						if(currentMtl!=null)
							materials.add(currentMtl);
						String mtName=line.split("[ ]+",2)[1];
						currentMtl=new Material(mtName);	
					}
					else
					if(line.startsWith("Ka")){
						String[] str=line.split("[ ]+");
						currentMtl.setAmbientColor(Float.parseFloat(str[1]), Float.parseFloat(str[2]), Float.parseFloat(str[3]));
					}
					else
					if(line.startsWith("Kd")){
						String[] str=line.split("[ ]+");
						currentMtl.setDiffuseColor(Float.parseFloat(str[1]), Float.parseFloat(str[2]), Float.parseFloat(str[3]));
					}
					else
					if(line.startsWith("Ks")){
						String[] str=line.split("[ ]+");
						currentMtl.setSpecularColor(Float.parseFloat(str[1]), Float.parseFloat(str[2]), Float.parseFloat(str[3]));
					}
					else
					if(line.startsWith("Tr") || line.startsWith("d")){
						String[] str=line.split("[ ]+");
						currentMtl.setAlpha(Float.parseFloat(str[1]));
					}
					else
					if(line.startsWith("Ns")){
						String[] str=line.split("[ ]+");
						currentMtl.setShine(Float.parseFloat(str[1]));
					}
					else
					if(line.startsWith("illum")){
						String[] str=line.split("[ ]+");
						currentMtl.setIllum(Integer.parseInt(str[1]));
					}
					else
					if(line.startsWith("map_Ka")){
						String[] str=line.split("[ ]+");
						currentMtl.setTextureFile(str[1]);
					}
				}
			}
			catch (Exception e) {
				// TODO: handle exception
			}
		}
		return materials;
	}
}

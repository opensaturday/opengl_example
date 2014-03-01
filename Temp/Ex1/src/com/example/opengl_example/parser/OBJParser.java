package com.example.opengl_example.parser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import com.example.opengl_example.R;

import android.content.Context;
import android.util.Log;



public class OBJParser {
	int numVertices=0;
	int numFaces=0;
	Context context;

	Vector<Short> faces=new Vector<Short>();
	Vector<Short> vtPointer=new Vector<Short>();
	Vector<Short> vnPointer=new Vector<Short>();
	Vector<Float> v=new Vector<Float>();
	Vector<Float> vn=new Vector<Float>();
	Vector<Float> vt=new Vector<Float>();
	Vector<TDModelPart> parts=new Vector<TDModelPart>();
	Vector<Material> materials=null;

	public OBJParser(Context ctx){
		context=ctx;
	}

	public TDModel parseOBJ(String fileName, int loadMode) {
		BufferedReader reader=null;
		String line = null;
		Material m=null;
		
		
		if(loadMode == 0) //파일 로드 모드
		{
			//reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		}
		else if(loadMode == 1) //리소스 로드 모드
		{
			InputStream fileIn = context.getResources().openRawResource(R.raw.camaro_obj);
			reader = new BufferedReader(
					new InputStreamReader(fileIn));
		}		
		
		try {
			while((line = reader.readLine()) != null) {
				Log.v("obj",line);
				if(line.startsWith("f")){
					processFLine(line);
				}
				else
					if(line.startsWith("vn")){
						processVNLine(line);
					}
					else
						if(line.startsWith("vt")){
							processVTLine(line);
						}
						else
							if(line.startsWith("v")){
								processVLine(line);
							}
							else
								if(line.startsWith("usemtl")){
									try{
									if(faces.size()!=0){
										TDModelPart model=new TDModelPart(faces, vtPointer, vnPointer, m,vn);
										parts.add(model);
									}
									String mtlName=line.split("[ ]+",2)[1];
									for(int i=0; i<materials.size(); i++){
										m=materials.get(i);
										if(m.getName().equals(mtlName)){
											break;
										}
										m=null;
									}
									faces=new Vector<Short>();
									vtPointer=new Vector<Short>();
									vnPointer=new Vector<Short>();
									}
									catch (Exception e) {
										// TODO: handle exception
									}
								}
								else
									if(line.startsWith("mtllib")){
										materials=MTLParser.loadMTL(line.split("[ ]+")[1],context);
										for(int i=0; i<materials.size(); i++){
											Material mat=materials.get(i);
											Log.v("materials",mat.toString());
										}
									}
			}
		} 		
		catch(IOException e){
			
		}
		if(faces!= null){
			TDModelPart model=new TDModelPart(faces, vtPointer, vnPointer, m,vn);
			parts.add(model);
		}
		TDModel t=new TDModel(v,vn,vt,parts);
		t.buildVertexBuffer();
		Log.v("models",t.toString());
		return t;
	}


	private void processVLine(String line){
		String [] tokens=line.split("[ ]+");
		int c=tokens.length; 
		for(int i=1; i<c; i++){ 
			v.add(Float.valueOf(tokens[i]));
		}
	}
	private void processVNLine(String line){
		String [] tokens=line.split("[ ]+");
		int c=tokens.length; 
		for(int i=1; i<c; i++){
			vn.add(Float.valueOf(tokens[i]));
		}
	}
	private void processVTLine(String line){
		String [] tokens=line.split("[ ]+");
		int c=tokens.length; 
		for(int i=1; i<c; i++){ 
			vt.add(Float.valueOf(tokens[i]));
		}
	}
	private void processFLine(String line){
		String [] tokens=line.split("[ ]+");
		int c=tokens.length;

		if(tokens[1].matches("[0-9]+")){//f: v
			if(c==4){//3 faces
				for(int i=1; i<c; i++){
					Short s=Short.valueOf(tokens[i]);
					s--;
					faces.add(s);
				}
			}
			else{//more faces
				Vector<Short> polygon=new Vector<Short>();
				for(int i=1; i<tokens.length; i++){
					Short s=Short.valueOf(tokens[i]);
					s--;
					polygon.add(s);
				}
				faces.addAll(Triangulator.triangulate(polygon));
			}
		}
		if(tokens[1].matches("[0-9]+/[0-9]+")){//if: v/vt
			if(c==4){//3 faces
				for(int i=1; i<c; i++){
					Short s=Short.valueOf(tokens[i].split("/")[0]);
					s--;
					faces.add(s);
					s=Short.valueOf(tokens[i].split("/")[1]);
					s--;
					vtPointer.add(s);
				}
			}
			else{//triangulate
				Vector<Short> tmpFaces=new Vector<Short>();
				Vector<Short> tmpVt=new Vector<Short>();
				for(int i=1; i<tokens.length; i++){
					Short s=Short.valueOf(tokens[i].split("/")[0]);
					s--;
					tmpFaces.add(s);
					s=Short.valueOf(tokens[i].split("/")[1]);
					s--;
					tmpVt.add(s);
				}
				faces.addAll(Triangulator.triangulate(tmpFaces));
				vtPointer.addAll(Triangulator.triangulate(tmpVt));
			}
		}
		if(tokens[1].matches("[0-9]+//[0-9]+")){//f: v//vn
			if(c==4){//3 faces
				for(int i=1; i<c; i++){
					Short s=Short.valueOf(tokens[i].split("//")[0]);
					s--;
					faces.add(s);
					s=Short.valueOf(tokens[i].split("//")[1]);
					s--;
					vnPointer.add(s);
				}
			}
			else{//triangulate
				Vector<Short> tmpFaces=new Vector<Short>();
				Vector<Short> tmpVn=new Vector<Short>();
				for(int i=1; i<tokens.length; i++){
					Short s=Short.valueOf(tokens[i].split("//")[0]);
					s--;
					tmpFaces.add(s);
					s=Short.valueOf(tokens[i].split("//")[1]);
					s--;
					tmpVn.add(s);
				}
				faces.addAll(Triangulator.triangulate(tmpFaces));
				vnPointer.addAll(Triangulator.triangulate(tmpVn));
			}
		}
		if(tokens[1].matches("[0-9]+/[0-9]+/[0-9]+")){//f: v/vt/vn

			if(c==4){//3 faces
				for(int i=1; i<c; i++){
					Short s=Short.valueOf(tokens[i].split("/")[0]);
					s--;
					faces.add(s);
					s=Short.valueOf(tokens[i].split("/")[1]);
					s--;
					vtPointer.add(s);
					s=Short.valueOf(tokens[i].split("/")[2]);
					s--;
					vnPointer.add(s);
				}
			}
			else{//triangulate
				Vector<Short> tmpFaces=new Vector<Short>();
				Vector<Short> tmpVn=new Vector<Short>();
				//Vector<Short> tmpVt=new Vector<Short>();
				for(int i=1; i<tokens.length; i++){
					Short s=Short.valueOf(tokens[i].split("/")[0]);
					s--;
					tmpFaces.add(s);
					//s=Short.valueOf(tokens[i].split("/")[1]);
					//s--;
					//tmpVt.add(s);
					//s=Short.valueOf(tokens[i].split("/")[2]);
					//s--;
					//tmpVn.add(s);
				}
				faces.addAll(Triangulator.triangulate(tmpFaces));
				vtPointer.addAll(Triangulator.triangulate(tmpVn));
				vnPointer.addAll(Triangulator.triangulate(tmpVn));
			}
		}
	}

}


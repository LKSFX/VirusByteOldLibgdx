package com.lksfx.virusByte.gameObject;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.lksfx.virusByte.gameObject.abstractTypes.VirusType;

public class VirusPool extends Pool<VirusType> {
	Class<? extends VirusType> type;
	
	public VirusPool(int min, int max, Class<? extends VirusType> virusType) {
		super(min, max);
		type = virusType;
	}
	
	public VirusType newObject() {
		try {
			return ClassReflection.newInstance(type); // old form (type.newInstance();)
		} catch (ReflectionException e) {
			e.printStackTrace();
		}
		return null;
	}
} 

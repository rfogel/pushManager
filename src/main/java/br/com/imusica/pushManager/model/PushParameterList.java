package br.com.imusica.pushManager.model;

import java.util.ArrayList;
import java.util.List;

public class PushParameterList
{
	private List<String> pushParameters;

	public PushParameterList() {
		this.pushParameters = new ArrayList<>();
	}

	public List<String> getPushParameters() {
		return pushParameters;
	}

	public void setPushParameters(List<String> pushParameters) {
		this.pushParameters = pushParameters;
	}
}


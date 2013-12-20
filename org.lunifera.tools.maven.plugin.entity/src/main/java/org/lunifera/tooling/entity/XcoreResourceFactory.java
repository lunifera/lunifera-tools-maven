package org.lunifera.tooling.entity;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xcore.resource.XcoreResource;

public class XcoreResourceFactory implements Resource.Factory {

	public Resource createResource(URI uri) {
		XcoreResource resource = new XcoreResource();
		resource.setURI(uri);
		return resource;
	}

}

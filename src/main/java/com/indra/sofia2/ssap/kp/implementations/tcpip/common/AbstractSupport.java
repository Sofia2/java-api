/*******************************************************************************
 * Copyright 2013-15 Indra Sistemas S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 ******************************************************************************/
package com.indra.sofia2.ssap.kp.implementations.tcpip.common;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSupport<T> {
	
	protected List<T> listeners = null;
	
	public AbstractSupport() {
		super();
		listeners = new ArrayList<T>( );
	}
  
    public void addListener(T l) {
        if(l == null) {
            throw new IllegalArgumentException("The listener can not be null");
        }
        synchronized(listeners) {
        	listeners.add(l);
        }
        
    }

    public void removeListener(T l) {
    	synchronized(listeners) {
    		listeners.remove(l);
    	}
    }
}

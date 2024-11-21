package org.debs.challenger2.dataset;

import org.debs.challenger2.rest.dao.Batch;

public interface IDataSelector {

    public boolean hasMoreElements();
    public Batch nextElement();
}

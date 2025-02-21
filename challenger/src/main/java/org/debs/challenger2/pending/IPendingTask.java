package org.debs.challenger2.pending;

import org.debs.challenger2.db.IQueries;

public interface IPendingTask {

    public void doPending(IQueries queries);
}

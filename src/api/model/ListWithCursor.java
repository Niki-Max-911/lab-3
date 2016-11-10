package api.model;

import java.util.Collection;
import java.util.LinkedList;

public class ListWithCursor<T> extends LinkedList<T> {
	public static final String EMPTY_CURSOR = "";
	private static final long serialVersionUID = 1L;
	private String nextCursor;

	public ListWithCursor() {
		super();
	}

	public ListWithCursor(Collection<T> e) {
		super(e);
	}

	public String getNextCursor() {
		if (nextCursor == null)
			return EMPTY_CURSOR;
		return nextCursor;
	}

	public boolean hasNextCursor() {
		return nextCursor != null && !nextCursor.equals(EMPTY_CURSOR);
	}

	public void setNextCursor(String nextCursor) {
		this.nextCursor = nextCursor;
	}
}

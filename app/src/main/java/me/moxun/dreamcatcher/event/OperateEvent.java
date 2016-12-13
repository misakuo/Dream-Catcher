package me.moxun.dreamcatcher.event;

/**
 * Created by moxun on 16/12/12.
 */

public class OperateEvent {
    public static final int TARGET_CONNECTOR = 0;
    public static final int TARGET_PROXY = 1;

    public int target = 0;
    public boolean active = false;
    public boolean error = false;
    public String msg = "";

    public OperateEvent(int target, boolean active, boolean error) {
        this(target, active, error, "");
    }

    public OperateEvent(int target, boolean active) {
        this(target, active, false);
    }

    public OperateEvent(int target, boolean active, boolean error, String msg) {
        this.target = target;
        this.active = active;
        this.error = error;
        this.msg = msg;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("OperateEvent{");
        sb.append("active=").append(active);
        sb.append(", target=").append(target);
        sb.append(", error=").append(error);
        sb.append('}');
        return sb.toString();
    }
}

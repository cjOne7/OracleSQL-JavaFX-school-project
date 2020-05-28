package sample;

import org.jetbrains.annotations.Nullable;

public enum Role {
    NEW(1, "New user"),
    STUDENT(2, "Student"),
    TEACHER(3, "Teacher"),
    ADMINISTRATOR(4, "Administrator"),
    MAIN_ADMIN(5, "Main Administrator");

    private int index;
    private String status;

    Role(final int index, final String status) {
        this.index = index;
        this.status = status;
    }

    public int getIndex() {
        return index;
    }

    @Nullable
    public static Role getRole(final int index) {
        for (int i = 0; i < Role.values().length; i++) {
            if (index == Role.values()[i].getIndex()) {
                return Role.values()[i];
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return status;
    }
}

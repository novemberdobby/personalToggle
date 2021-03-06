package novemberdobby.teamcity.personalToggle;

public enum ToggleSetting {
    Unset("-"),
    Only("Only runs personal builds"),
    Never("Never runs personal builds");

    private String m_str;

    private ToggleSetting(String str) {
        m_str = str;
    }

    @Override
    public String toString() {
        return m_str;
    }
}
package ru.finik.dwclient.serverconnection;

public interface Session {
    String idOfSession = null;
    int numOfConn = 0;
    boolean isActive = false;
    int lengthOfTrack = 0;
    int position = 0;

    public void setIdOfSession(String idOfSession);
    public void setNumOfConn(int numOfConn);
    public void setActive(boolean isActive);
    public void setPosition(int position);
    public void setLengthOfTrack(int lengthOfTrack);


}

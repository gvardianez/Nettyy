package ru.alov.network.cloud.common.file_transfer.protocol;

public enum CommandType {

    AUTH((byte) 18),

    AUTH_OK((byte) 19),

    FILES_LIST((byte) 20),

    REFRESH((byte) 21),

    FILES_DELETE((byte) 22),

    CREATE_DIRECTORY((byte) 23),

    ERROR((byte) 24),

    OPEN_DIRECTORY((byte) 25),

    GO_BACK_DIRECTORY((byte) 26),

    FILES_REQUEST((byte) 27),

    SHARE_FILE((byte) 28),

    FILE_PACKAGE_REQUEST((byte) 29),

    FILE_PACKAGE_RESPONSE((byte) 30),

    FILE_PART((byte) 31);

    private final byte b;

    CommandType(byte aByte){
        this.b = aByte;
    }

    public byte getByte() {
        return b;
    }
}

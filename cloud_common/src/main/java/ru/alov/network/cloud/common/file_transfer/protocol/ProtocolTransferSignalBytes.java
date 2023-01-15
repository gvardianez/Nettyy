package ru.alov.network.cloud.common.file_transfer.protocol;

public enum ProtocolTransferSignalBytes {

    FILE_SEND_BYTES(new byte[]{1,3,5,7,9,11}),

    FILE_REQUEST_BYTES(new byte[]{2,4,6,8,10,12});

    private final byte[] bytes;

    private ProtocolTransferSignalBytes(byte[] bytes){
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }
}

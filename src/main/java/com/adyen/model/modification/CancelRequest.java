package com.adyen.model.modification;

public class CancelRequest extends AbstractModificationRequest<CancelRequest> {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CancelRequest {\n");

        sb.append(super.toString());
        sb.append("}");
        return sb.toString();
    }

}

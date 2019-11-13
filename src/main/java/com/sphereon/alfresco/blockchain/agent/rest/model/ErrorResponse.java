package com.sphereon.alfresco.blockchain.agent.rest.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApiModel(
        description = "The error response"
)
public class ErrorResponse {
    @SerializedName("errors")
    private List<Error> errors = null;

    public ErrorResponse() {
    }

    public ErrorResponse errors(List<Error> errors) {
        this.errors = errors;
        return this;
    }

    public ErrorResponse addErrorsItem(Error errorsItem) {
        if (this.errors == null) {
            this.errors = new ArrayList();
        }

        this.errors.add(errorsItem);
        return this;
    }

    @ApiModelProperty("")
    public List<Error> getErrors() {
        return this.errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            ErrorResponse errorResponse = (ErrorResponse) o;
            return Objects.equals(this.errors, errorResponse.errors);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.errors});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ErrorResponse {\n");
        sb.append("    errors: ").append(this.toIndentedString(this.errors)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}

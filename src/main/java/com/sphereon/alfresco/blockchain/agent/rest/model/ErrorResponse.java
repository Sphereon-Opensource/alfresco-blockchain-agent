package com.sphereon.alfresco.blockchain.agent.rest.model;

import com.google.common.base.MoreObjects;
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
    private List<Error> errors;

    public ErrorResponse() {
        this.errors = new ArrayList<>();
    }

    public ErrorResponse errors(List<Error> errors) {
        this.errors = errors;
        return this;
    }

    public ErrorResponse addErrorsItem(Error errorsItem) {
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

    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }

        final var other = (ErrorResponse) object;
        return Objects.equals(this.errors, other.errors);
    }

    public int hashCode() {
        return Objects.hash(this.errors);
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("errors", this.errors)
                .toString();
    }
}

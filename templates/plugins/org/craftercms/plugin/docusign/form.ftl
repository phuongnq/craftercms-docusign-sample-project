<#if contentModel.usePluginTheme_b?? && contentModel.usePluginTheme_b>
    <link rel="stylesheet" href="/static-assets/plugins/org/craftercms/plugin/docusign/css/docusign-form.css" />
</#if>

<#assign validateForm = contentModel.usePluginScript_b?? && contentModel.usePluginScript_b />

<form
        id="${contentModel.formId_s}"
        action="${contentModel.url_s}"
        method="post"
        class="contact-form <#if validateForm>needs-validation</#if>"
        <#if validateForm>novalidate</#if>
>
    <#if contentModel.title_s??>
        <div class="form-section">
            <h2 class="form-title">${contentModel.title_s}</h2>
        </div>
    </#if>

    <div class="form-section">
        <label for="${contentModel.formId_s}-email" class="form-label">
            ${contentModel.emailLabel_s}
            <#if validateForm>
                <span class="form-field-required-indicator" />
            </#if>
        </label>
        <input id="${contentModel.formId_s}-email" name="email" class="form-control" required></input>
    </div>

    <div class="form-section">
        <label for="${contentModel.formId_s}-name" class="form-label">
            ${contentModel.nameLabel_s}
            <#if validateForm>
                <span class="form-field-required-indicator" />
            </#if>
        </label>
        <input id="${contentModel.formId_s}-name" name="name" class="form-control" required></input>
    </div>

    <input type="hidden" name="formId" value="${contentModel.formId_s}"/>
    <input type="submit" class="submit-btn" value="${contentModel.submitLabel_s}"/>
</form>

<#if validateForm>
    <script src="/static-assets/plugins/org/craftercms/plugin/docusign/js/validate-form.js"></script>
</#if>
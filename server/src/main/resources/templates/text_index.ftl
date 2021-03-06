<@ie.page "Text Repository">

<h2>Upload a text</h2>
<form id="upload" name="upload" method="post" enctype="multipart/form-data">
  <p>
    <label for="file">File:</label> <input type="file" name="file" id="file"/>
    <label for="fileType">Type:</label> <select id="fileType" name="fileType">
    <option value="TXT" selected="selected">Plain Text</option>
    <option value="XML">XML</option>
  </select>
    <label for="fileEncoding">Encoding:</label> <select id="fileEncoding" name="fileEncoding">
    <option value="UTF-8">UTF-8</option>
    <option value="ISO-8859-1">ISO-8859-1</option>
    <option value="UTF-16">UTF-16</option>
  </select>
    <input type="submit" value="Upload"></p>
</form>

<h2>Type/Paste a plain text</h2>

<form id="paste" name="paste" method="post">
  <p><textarea id="text" rows="10" style="width: 100%"></textarea><br><input type="submit" value="Send"></p>
</form>

<script type="text/javascript">
  YUI().use("node", "event", function (Y) {
    Y.on("domready", function () {
      var file = Y.one("#file");
      var fileType = Y.one("#fileType");
      var fileEncoding = Y.one("#fileEncoding");
      var text = Y.one("#text");

      Y.on("change", function () {
        fileEncoding.set("disabled", this.get("value") == "XML");
      }, fileType);

      Y.on("change", function () {
        if (this.get("value").match(/\.xml$/i)) {
          fileType.set("value", "XML");
          fileEncoding.set("disabled", true);
        } else {
          fileType.set("value", "TXT");
          fileEncoding.set("disabled", false);
        }
      }, file);

      Y.on("submit", function(e) {
        var fileValue = file.get("value");
        if (fileValue == null || fileValue.trim().length == 0) {
          e.preventDefault();
        }
      }, "#upload");

      Y.on("submit", function(e) {
        e.preventDefault();
        var textValue = text.get("value");
        if (textValue == null || textValue.trim().length == 0) {
          return;
        }


      }, "#paste");
    });
  });
</script>
</@ie.page>
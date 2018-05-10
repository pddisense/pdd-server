$(function () {
  let index = undefined;

  function buildIndex(callback) {
    if (typeof index === 'undefined') {
      fetch('/content.json')
        .then(function (response) {
          return response.json();
        })
        .then(function (data) {
          index = lunr(function () {
            this.ref('url');
            this.field('title');
            this.field('content');
            data.forEach(function (doc) {
              this.add(doc)
            }, this)
          });
          callback();
        });
    } else {
      callback();
    }
  }

  $('#search-form').submit(function (e) {
    e.preventDefault();
    buildIndex(function() {
      const q = $('#search-form input[type=text]').val();
      console.log(index.search(q));
    });
  });
});

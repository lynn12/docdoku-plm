/*global define*/
define([
        "backbone",
        "mustache",
        "text!templates/controls_infos_modal.html"
    ],

    function (Backbone, Mustache, template) {

        var ControlsInfosModalView = Backbone.View.extend({

            events: {
                "hidden #controlsInfosModal": "onHidden"
            },

            initialize: function () {
                _.bindAll(this);
            },

            render: function () {
                if (this.options.isPLC) {
                    this.$el.html(Mustache.render(template, {i18n: APP_CONFIG.i18n, isPLC: true}));
                } else if (this.options.isTBC) {
                    this.$el.html(Mustache.render(template, {i18n: APP_CONFIG.i18n, isTBC: true}));
                } else if (this.options.isORB) {
                    this.$el.html(Mustache.render(template, {i18n: APP_CONFIG.i18n, isORC: true}));
                }

                this.$modal = this.$("#controlsInfosModal");

                return this;
            },

            openModal: function () {
                this.$modal.modal('show');
            },

            closeModal: function () {
                this.$modal.modal('hide');
            },

            onHidden: function () {
                this.remove();
            }

        });

        return ControlsInfosModalView;
    }
);
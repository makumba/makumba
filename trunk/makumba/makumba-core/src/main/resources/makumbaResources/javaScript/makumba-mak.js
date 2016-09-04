/* ========================================================================== *
 *  The Makumba Javascript namespace                                          *
 *  ==============================                                            *
 *  In order to not pollute the environment with many random functions and    *
 *  variables, we have this global variable.                                  *
 *  This global variable contains: any kind of constant which is needed in    *
 *  more than one JS file or any kind of variable which is relevant to the    *
 *  framework as a whole.                                                     *
 * ========================================================================== */

+function () {
  'use strict';
	var Mak = function () {
		return new Mak.init();
	};

	Mak.init = function() {
		this.fn = Mak.init.prototype;
	};

	window.Mak = Mak();
}();
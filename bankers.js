if (typeof String.prototype.reverse == "undefined")
    String.prototype.reverse = function() {
        return this.split("").reverse().join("");
    };

var Bankers = {
    next: function(b) {
        var z = "", y = "", next = "";
        
        var comp = /(1*)(0*)(1[01]*)?/.exec(b.reverse());
        comp.reverse();
        
        if (comp.length > 1 && comp[1])
            z = comp[1];
        if (comp.length > 2 && comp[2])
            y = comp[2];
        if (comp[0])
            next = comp[0].reverse().substring(0, comp[0].length - 1) + "0";
            
        next += y + "1";
        if (z.length > 0)
            next += z.substring(1);
            
        return next;
    },
    to: function(a, n) {
        if (typeof a == "string") {
            n = n || a.length;
            a = parseInt(a, 2);
        }
        if (!n)
            throw "Unable to reach Banker's number without length.";
        if (a == 0) {
            var b = "";
            while (n--) b += "0";
            return b;
        }
        
        var cn = 0, b = [], c = [], e = [a], i = 0;
        do {
            e[0] -= Binom.choose(n, cn++);
        } while (Binom.choose(n, cn) <= e[0]);
        if (Binom.choose(n - 1, cn - 1) <= e[i]) {
            b[i] = "0";
            c[i] = 0;
        } else {
            b[i] = "1";
            c[i] = 1;
        }
        
        while (cn > c[i] && i < n - 1) {
            if (b[i] == "0")
                e[i+1] = e[i] - Binom.choose(n - i - 1, cn - c[i] - 1);
            else
                e[i+1] = e[i];
            if (e[i+1] === 0 || Binom.choose(n - i - 2, cn - c[i] - 1) > e[i+1]) { 
                b[i+1] = "1";
                c[i+1] = c[i] + 1;
            } else {
                b[i+1] = "0";
                c[i+1] = c[i];
            }
            i++;
        }
        while (i < n - 1)
            b[++i] = "0";
        
        return b.join("");
    },
    from: function(b) {
        var bits = b.split("");
        var n = b.length, a = 0, c = 0, cn = (b.match(/1/g) || []).length;
        for (var i = 0; i < bits.length && c < cn; i++) {
            if (bits[i] == "1")
                a += Binom.choose(n, c++);
            else
                a += Binom.choose(n - i - 1, cn - c - 1);
        }
        return a;
    }
};
Bankers.version = "0.1";

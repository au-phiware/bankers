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
        var c = 0, b = [], e = a, i = 0;
        if (a == 0) {
            b = "";
            while (n--) b += "0";
            return b;
        }
        
        do {
            e -= Binom.choose(n, c++);
        } while (Binom.choose(n, c) <= a);
        if (Binom.choose(n - 1, c--))
            b[i] = "1";
        else
            b[i] = "0";
        
        while (c >= 0) {
            if (b[i] == "0")
                e -= Binom.choose(n - i - 1, c);
            if (Binom.choose(n - i, c) > e) { 
                b[i++] = "1";
                c--;
            } else
                b[i++] = "0";
        }
        while (i < n) {
            b[i++] = "0";
        }
        
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
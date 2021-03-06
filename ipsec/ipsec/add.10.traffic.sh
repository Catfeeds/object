
_reqid=1
_local="10.3.201.0/24"
_local_ip="172.20.2.102"
_remote="10.0.0.0/16"
_remote_ip="42.62.5.229"

ip xfrm policy add \
   src ${_remote} dst ${_local} \
        dir fwd priority 2915 ptype main \
        tmpl src ${_remote_ip} dst ${_local_ip} \
                proto esp reqid ${_reqid} mode tunnel						

ip xfrm policy add \
   src ${_remote} dst ${_local} \
        dir in priority 2915 ptype main \
        tmpl src ${_remote_ip} dst ${_local_ip} \
                proto esp reqid ${_reqid} mode tunnel						

ip xfrm policy add \
src ${_local} dst ${_remote} \
        dir out priority 2915 ptype main \
        tmpl src ${_local_ip} dst ${_remote_ip} \
                proto esp reqid ${_reqid} mode tunnel
                
                

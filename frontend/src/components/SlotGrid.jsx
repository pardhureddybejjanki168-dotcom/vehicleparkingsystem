import React from 'react';
import { Car, Bike, Zap, CheckCircle2, XCircle, AlertTriangle } from 'lucide-react';

const SlotGrid = ({ slots = [], onSelectSlot, selectedSlotId, readOnly = false }) => {
  const getStatusClass = (status) => {
    switch (status) {
      case 'AVAILABLE':
        return 'slot-available';
      case 'OCCUPIED':
        return 'slot-occupied';
      case 'OUT_OF_SERVICE':
        return 'slot-out-of-service';
      default:
        return '';
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'AVAILABLE':
        return <CheckCircle2 size={14} color="#10b981" />;
      case 'OCCUPIED':
        return <XCircle size={14} color="#ef4444" />;
      case 'OUT_OF_SERVICE':
        return <AlertTriangle size={14} color="#64748b" />;
      default:
        return null;
    }
  };

  const getVehicleIcon = (type) => {
    switch (type) {
      case 'EV':
        return <Zap size={12} />;
      case 'BIKE':
        return <Bike size={12} />;
      default:
        return <Car size={12} />;
    }
  };

  if (!slots || slots.length === 0) {
    return (
      <div style={{ textAlign: 'center', padding: '3rem', color: '#64748b' }}>
        <p>No parking slots match the current filter criteria.</p>
      </div>
    );
  }

  return (
    <div className="parking-grid">
      {slots.map((slot) => {
        const isSelected = selectedSlotId === slot.id;
        const isClickable = !readOnly && slot.status === 'AVAILABLE';

        return (
          <div
            key={slot.id}
            className={`slot-card ${getStatusClass(slot.status)} ${isSelected ? 'selected' : ''}`}
            style={{
              outline: isSelected ? '3px solid #2563eb' : 'none',
              cursor: isClickable ? 'pointer' : 'default',
            }}
            onClick={() => {
              if (onSelectSlot && (isClickable || readOnly)) {
                onSelectSlot(slot);
              }
            }}
            role="button"
            tabIndex={0}
            aria-label={`Slot ${slot.slotNumber}, status ${slot.status}`}
          >
            <div className="slot-number">{slot.slotNumber}</div>
            <div className="slot-zone-tag">{slot.zoneFloor || slot.zoneName}</div>
            <div className="slot-type-tag" style={{ display: 'flex', alignItems: 'center', gap: '3px' }}>
              {getVehicleIcon(slot.status === 'OCCUPIED' && slot.occupiedVehicleType ? slot.occupiedVehicleType : slot.vehicleTypeAllowed)}
              <span>
                {slot.status === 'OCCUPIED' && slot.occupiedVehicleType
                  ? slot.occupiedVehicleType
                  : slot.vehicleTypeAllowed}
              </span>
            </div>
            {slot.status === 'OCCUPIED' && slot.occupiedVehiclePlate && (
              <div style={{ fontSize: '0.65rem', fontWeight: 700, color: '#991b1b', marginTop: '2px', letterSpacing: '0.02em' }}>
                {slot.occupiedVehiclePlate}
              </div>
            )}
            <div className="slot-status-label" style={{ display: 'flex', alignItems: 'center', gap: '3px' }}>
              {getStatusIcon(slot.status)}
              <span>{slot.status === 'AVAILABLE' ? 'Available' : slot.status === 'OCCUPIED' ? 'Occupied' : 'Out of Service'}</span>
            </div>
          </div>
        );
      })}
    </div>
  );
};

export default SlotGrid;
